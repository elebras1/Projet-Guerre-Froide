package com.populaire.projetguerrefroide.system.rgo;

import com.populaire.projetguerrefroide.simulation.EcsConstants;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class RGOProductionSystem {

    public RGOProductionSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        ecsWorld.system("RGOProductionSystem")
            .kind(phaseId)
            .with(ResourceGathering.class)
            .with(ResourceGatheringType.class).up(ecsConstants.rgoType())
            .with(Province.class)
            .with(ProvinceInfrastructure.class)
            .multiThreaded()
            .iter(this::produce);
    }

    private void produce(Iter iter) {
        Field<ResourceGathering> resourceGatheringField = iter.field(ResourceGathering.class, 0);
        ResourceGatheringTypeView resourceGatheringTypeData = iter.field(ResourceGatheringType.class, 1).getView(0);
        int workforce = resourceGatheringTypeData.workforce();
        float workerPopTypeRatio = resourceGatheringTypeData.workerPopTypeRatio();
        float slavePopTypeRatio = resourceGatheringTypeData.slavePopTypeRatio();
        float workerEffectMultiplier = resourceGatheringTypeData.workerEffectMultiplier();
        float slaveEffectMultiplier = resourceGatheringTypeData.slaveEffectMultiplier();
        Field<Province> provinceField = iter.field(Province.class, 2);
        Field<ProvinceInfrastructure> provinceInfrastructureField = iter.field(ProvinceInfrastructure.class, 3);
        if (iter.count() == 0) {
            return;
        }
        ResourceGatheringView firstResourceGathering = resourceGatheringField.getMutView(0);
        long goodId = firstResourceGathering.goodId();
        int goodIndex = firstResourceGathering.goodIndex();
        float goodValue = iter.stage().obtainEntityView(goodId).<GoodView>getView(Good.class).value();

        long countryId = 0;
        float rgoOutputModifier = 0f;
        float rgoProductivity = 0f;
        for(int i = 0; i < iter.count(); i++) {
            ResourceGatheringMutView resourceGathering = resourceGatheringField.getMutView(i);
            ProvinceView province = provinceField.getMutView(i);
            if (province.ownerId() != countryId) {
                countryId = province.ownerId();
                EntityView country = iter.stage().obtainEntityView(countryId);
                rgoOutputModifier = country.<CountryProductionPolicyView>getView(CountryProductionPolicy.class).rgoOutputModifier();
                rgoProductivity = country.<CountryProductionTechnologyView>getView(CountryProductionTechnology.class).rgoProductivities(goodIndex);
            }

            float endowment = resourceGathering.endowment();
            float capacity = endowment * workforce;
            float levelProduction = goodValue * resourceGathering.efficiency() * (1f + rgoOutputModifier + rgoProductivity);
            float baseProduction = endowment * levelProduction;

            int targetWorkers = (int) (capacity * workerPopTypeRatio);
            int targetSlaves = (int) (capacity * slavePopTypeRatio);

            float slaveFulfillment = (float) resourceGathering.slaveAmount() / Math.max(1, targetSlaves);

            // production = value x M x endowment^alpha x levels^(1 - alpha), levels = workers / (workforce x worker ratio)
            // (decision 184); alpha = 0 keeps the former rule, proportional to the workers
            float workerLevels = resourceGathering.workerAmount() / Math.max(1f, workforce * workerPopTypeRatio);
            float alpha = resourceGathering.endowmentShare();
            float scale = alpha > 0f ? (float) (Math.pow(endowment, alpha) * Math.pow(workerLevels, 1f - alpha)) : workerLevels;
            float coreProduction = scale * levelProduction * workerEffectMultiplier;

            float maxBonus = slaveEffectMultiplier - 1.0f;
            float currentSlaveBonus = 1.0f + (slaveFulfillment * maxBonus);

            ProvinceInfrastructureView provinceInfrastructure = provinceInfrastructureField.getMutView(i);
            float infrastructure = 1f + provinceInfrastructure.productionBonus();
            resourceGathering.production(coreProduction * currentSlaveBonus * infrastructure);
            resourceGathering.workerOutput(targetWorkers > 0 ? baseProduction * workerEffectMultiplier * currentSlaveBonus * infrastructure / targetWorkers : 0f);
        }
    }
}

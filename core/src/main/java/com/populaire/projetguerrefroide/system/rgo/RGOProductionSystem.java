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

            int maxCapacity = resourceGathering.size() * workforce;
            float baseProduction = resourceGathering.size() * goodValue * resourceGathering.efficiency() * (1f + rgoOutputModifier + rgoProductivity);

            int targetWorkers = (int) (maxCapacity * workerPopTypeRatio);
            int targetSlaves = (int) (maxCapacity * slavePopTypeRatio);

            float workerFulfillment = (float) resourceGathering.workerAmount() / Math.max(1, targetWorkers);
            float slaveFulfillment = (float) resourceGathering.slaveAmount() / Math.max(1, targetSlaves);

            float coreProduction = workerFulfillment * baseProduction * workerEffectMultiplier;

            float maxBonus = slaveEffectMultiplier - 1.0f;
            float currentSlaveBonus = 1.0f + (slaveFulfillment * maxBonus);

            ProvinceInfrastructureView provinceInfrastructure = provinceInfrastructureField.getMutView(i);
            float infrastructure = 1f + provinceInfrastructure.productionBonus();
            resourceGathering.production(coreProduction * currentSlaveBonus * infrastructure);
            resourceGathering.workerOutput(targetWorkers > 0 ? baseProduction * workerEffectMultiplier * currentSlaveBonus * infrastructure / targetWorkers : 0f);
        }
    }
}

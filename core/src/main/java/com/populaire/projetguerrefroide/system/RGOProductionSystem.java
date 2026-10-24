package com.populaire.projetguerrefroide.system;

import com.populaire.projetguerrefroide.util.EcsConstants;
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
        float goodValue = iter.world().obtainEntityView(goodId).<GoodView>getView(Good.class).value();

        long countryId = 0;
        float rgoOutputModifier = 0f;
        for(int i = 0; i < iter.count(); i++) {
            ResourceGatheringMutView resourceGathering = resourceGatheringField.getMutView(i);
            ProvinceView province = provinceField.getMutView(i);
            if (province.ownerId() != countryId) {
                countryId = province.ownerId();
                rgoOutputModifier = iter.world().obtainEntityView(countryId).<CountryProductionPolicyView>getView(CountryProductionPolicy.class).rgoOutputModifier();
            }

            int maxCapacity = resourceGathering.size() * workforce;
            float baseProduction = resourceGathering.size() * goodValue * resourceGathering.efficiency() * (1f + rgoOutputModifier);

            int targetWorkers = (int) (maxCapacity * workerPopTypeRatio);
            int targetSlaves = (int) (maxCapacity * slavePopTypeRatio);

            float workerFulfillment = (float) resourceGathering.workerAmount() / Math.max(1, targetWorkers);
            float slaveFulfillment = (float) resourceGathering.slaveAmount() / Math.max(1, targetSlaves);

            float coreProduction = workerFulfillment * baseProduction * workerEffectMultiplier;

            float maxBonus = slaveEffectMultiplier - 1.0f;
            float currentSlaveBonus = 1.0f + (slaveFulfillment * maxBonus);

            ProvinceInfrastructureView provinceInfrastructure = provinceInfrastructureField.getMutView(i);
            resourceGathering.production(coreProduction * currentSlaveBonus * (1f + provinceInfrastructure.productionBonus()));
        }
    }
}

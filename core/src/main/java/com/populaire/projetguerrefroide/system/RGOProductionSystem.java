package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class RGOProductionSystem {

    public RGOProductionSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("RGOProductionSystem")
            .kind(phaseId)
            .with(ResourceGathering.class)
            .multiThreaded()
            .iter(this::produce);
    }

    private void produce(Iter iter) {
        long resourceGatheringTypeId = 0;
        ResourceGatheringTypeView resourceGatheringTypeData = null;

        Field<ResourceGathering> resourceGatheringField = iter.field(ResourceGathering.class, 0);

        for(int i = 0; i < iter.count(); i++) {
            ResourceGatheringView resourceGathering = resourceGatheringField.getMutView(i);

            if(resourceGathering.typeId() != resourceGatheringTypeId) {
                resourceGatheringTypeId = resourceGathering.typeId();
                EntityView resourceGatheringType = iter.world().obtainEntityView(resourceGatheringTypeId);
                resourceGatheringTypeData = resourceGatheringType.getMutView(ResourceGatheringType.class);
            }

            int maxCapacity = resourceGathering.size() * resourceGatheringTypeData.workforce();
            float baseProduction = resourceGathering.size() * resourceGathering.goodAmount();

            int targetWorkers = (int) (maxCapacity * resourceGatheringTypeData.workerPopTypeRatio());
            int targetSlaves = (int) (maxCapacity * resourceGatheringTypeData.slavePopTypeRatio());

            float workerFulfillment = (float) resourceGathering.workerAmount() / Math.max(1, targetWorkers);
            float slaveFulfillment = (float) resourceGathering.slaveAmount() / Math.max(1, targetSlaves);

            float coreProduction = workerFulfillment * baseProduction * resourceGatheringTypeData.workerEffectMultiplier();

            float maxBonus = resourceGatheringTypeData.slaveEffectMultiplier() - 1.0f;
            float currentSlaveBonus = 1.0f + (slaveFulfillment * maxBonus);

            resourceGathering.production(coreProduction * currentSlaveBonus);
        }
    }
}

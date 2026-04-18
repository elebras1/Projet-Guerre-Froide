package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.ResourceGathering;
import com.populaire.projetguerrefroide.component.ResourceGatheringType;
import com.populaire.projetguerrefroide.component.ResourceGatheringTypeView;
import com.populaire.projetguerrefroide.component.ResourceGatheringView;

public class RGOProduceSystem {

    public RGOProduceSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("RGOProduceSystem")
            .kind(phaseId)
            .with(ResourceGathering.class)
            .iter(this::produce);
    }

    private void produce(Iter iter) {
        Field<ResourceGathering> resourceGatheringField = iter.field(ResourceGathering.class, 0);

        for(int i = 0; i < iter.count(); i++) {
            ResourceGatheringView resourceGathering = resourceGatheringField.getMutView(i);

            EntityView resourceGatheringType = iter.world().obtainEntityView(resourceGathering.typeId());
            ResourceGatheringTypeView resourceGatheringTypeData = resourceGatheringType.getMutView(ResourceGatheringType.class);

            int maxCapacity = resourceGathering.size() * resourceGatheringTypeData.workforce();
            float baseProduction = resourceGathering.size() * resourceGathering.goodAmount();

            int targetWorkers = (int) (maxCapacity * resourceGatheringTypeData.workerPopTypeRatio());
            int targetSlaves = (int) (maxCapacity * resourceGatheringTypeData.slavePopTypeRatio());

            float workerFulfillment = (float) resourceGathering.workerAmount() / Math.max(1, targetWorkers);
            float slaveFulfillment = (float) resourceGathering.slaveAmount() / Math.max(1, targetSlaves);

            float coreProduction = workerFulfillment * baseProduction * resourceGatheringTypeData.workerEffectMultiplier();

            float maxBonus = resourceGatheringTypeData.slaveEffectMultiplier() - 1.0f;
            float currentSlaveBonus = 1.0f + (slaveFulfillment * maxBonus);

            float finalProduction = coreProduction * currentSlaveBonus;
            resourceGathering.production(finalProduction);
        }
    }
}

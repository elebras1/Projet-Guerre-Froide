package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class RGOHireInitializationSystem {

    public RGOHireInitializationSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("RGOHireInitializationSystem")
            .kind(phaseId)
            .with(ResourceGathering.class)
            .with(Demographics.class)
            .iter(this::hire);
    }

    private void hire(Iter iter) {
        Field<ResourceGathering> resourceGatheringField = iter.field(ResourceGathering.class, 0);
        Field<Demographics> demographicsField = iter.field(Demographics.class, 1);

        for(int i = 0; i < iter.count(); i++) {
            ResourceGatheringView resourceGathering = resourceGatheringField.getMutView(i);
            DemographicsView demographics = demographicsField.getMutView(i);

            EntityView resourceGatheringType = iter.world().obtainEntityView(resourceGathering.typeId());
            ResourceGatheringTypeView resourceGatheringTypeData = resourceGatheringType.getMutView(ResourceGatheringType.class);

            int maxCapacity = resourceGathering.size() * resourceGatheringTypeData.workforce();

            int targetWorkers = (int) (maxCapacity * resourceGatheringTypeData.workerPopTypeRatio());
            int targetSlaves = (int) (maxCapacity * resourceGatheringTypeData.slavePopTypeRatio());

            int workerIndex = resourceGatheringTypeData.workerPopTypeIndex();
            int slaveIndex = resourceGatheringTypeData.slavePopTypeIndex();

            int workerAvailable = demographics.totalByPopType(workerIndex);
            int slaveAvailable = demographics.totalByPopType(slaveIndex);

            int workerAmount = Math.min(targetWorkers, workerAvailable);
            int slaveAmount = Math.min(targetSlaves, slaveAvailable);

            resourceGathering.workerAmount(workerAmount);
            resourceGathering.slaveAmount(slaveAmount);
        }
    }
}

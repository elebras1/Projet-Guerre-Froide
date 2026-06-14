package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class RGOEmploymentSystem {

    public RGOEmploymentSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("RGOEmploymentSystem")
            .kind(phaseId)
            .with(ResourceGathering.class)
            .with(Demographics.class)
            .multiThreaded()
            .iter(this::hire);
    }

    private void hire(Iter iter) {
        long rgoTypeId = 0;
        ResourceGatheringTypeView rgoTypeData = null;

        Field<ResourceGathering> resourceGatheringField = iter.field(ResourceGathering.class, 0);
        Field<Demographics> demographicsField = iter.field(Demographics.class, 1);
        for(int i = 0; i < iter.count(); i++) {
            ResourceGatheringView resourceGathering = resourceGatheringField.getMutView(i);
            DemographicsView demographics = demographicsField.getMutView(i);

            if(resourceGathering.typeId() != rgoTypeId) {
                rgoTypeId = resourceGathering.typeId();
                EntityView rgoType = iter.world().obtainEntityView(rgoTypeId);
                rgoTypeData = rgoType.getMutView(ResourceGatheringType.class);
            }

            int maxCapacity = resourceGathering.size() * rgoTypeData.workforce();

            int targetWorkers = (int) (maxCapacity * rgoTypeData.workerPopTypeRatio());
            int targetSlaves = (int) (maxCapacity * rgoTypeData.slavePopTypeRatio());

            int workerIndex = rgoTypeData.workerPopTypeIndex();
            int slaveIndex = rgoTypeData.slavePopTypeIndex();

            int workerAvailable = demographics.totalByPopType(workerIndex);
            int slaveAvailable = demographics.totalByPopType(slaveIndex);

            int workerAmount = Math.min(targetWorkers, workerAvailable);
            int slaveAmount = Math.min(targetSlaves, slaveAvailable);

            resourceGathering.workerAmount(workerAmount);
            resourceGathering.slaveAmount(slaveAmount);
        }
    }
}

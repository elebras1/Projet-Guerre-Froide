package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;

public class RGOEmploymentInitializationSystem {

    public RGOEmploymentInitializationSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        ecsWorld.system("RGOEmploymentInitializationSystem")
            .kind(phaseId)
            .with(ResourceGathering.class)
            .with(Demographics.class)
            .with(ResourceGatheringType.class).up(ecsConstants.rgoType())
            .multiThreaded()
            .iter(this::initialize);
    }

    private void initialize(Iter iter) {
        Field<ResourceGathering> resourceGatheringField = iter.field(ResourceGathering.class, 0);
        Field<Demographics> demographicsField = iter.field(Demographics.class, 1);
        ResourceGatheringTypeView rgoTypeData = iter.field(ResourceGatheringType.class, 2).getView(0);
        int workforce = rgoTypeData.workforce();
        float workerPopTypeRatio = rgoTypeData.workerPopTypeRatio();
        float slavePopTypeRatio = rgoTypeData.slavePopTypeRatio();
        int workerIndex = rgoTypeData.workerPopTypeIndex();
        int slaveIndex = rgoTypeData.slavePopTypeIndex();
        for (int i = 0; i < iter.count(); i++) {
            ResourceGatheringMutView resourceGathering = resourceGatheringField.getMutView(i);
            DemographicsView demographics = demographicsField.getMutView(i);
            int maxCapacity = resourceGathering.size() * workforce;
            int targetWorkers = (int) (maxCapacity * workerPopTypeRatio);
            int targetSlaves = (int) (maxCapacity * slavePopTypeRatio);
            resourceGathering.workerAmount(Math.min(targetWorkers, demographics.totalByPopType(workerIndex)));
            resourceGathering.slaveAmount(Math.min(targetSlaves, demographics.totalByPopType(slaveIndex)));
        }
    }
}

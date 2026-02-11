package com.populaire.projetguerrefroide.system.economy;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.util.FlecsConstants;
import com.populaire.projetguerrefroide.component.*;

public class ResourceGatheringOperationHireSystem {

    public ResourceGatheringOperationHireSystem(World ecsWorld) {
        ecsWorld.system("RGOHireSystem").kind(FlecsConstants.EcsOnUpdate).with(Province.class).with(ResourceGathering.class).with(PopulationDistribution.class).multiThreaded().iter(this::hire);
    }

    public void hire(Iter iter) {
        Field<ResourceGathering> resourceGatheringField = iter.field(ResourceGathering.class, 1);
        Field<PopulationDistribution> populationDistributionField = iter.field(PopulationDistribution.class, 2);
        for (int i = 0; i < iter.count(); i++) {
            ResourceGatheringView resourceGatheringView = resourceGatheringField.getMutView(i);
            PopulationDistributionView popDistribution = populationDistributionField.getMutView(i);

            int size = resourceGatheringView.size();
            int workforce = resourceGatheringView.workforce();
            int maxWorkers = size * workforce;

            for (int employeeIndex = 0; employeeIndex < resourceGatheringView.employeePopTypeIndexesLength() && resourceGatheringView.employeePopTypeIndexes(employeeIndex) >= 0; employeeIndex++) {
                int popTypeIndex = resourceGatheringView.employeePopTypeIndexes(employeeIndex);
                int popTypeValue = popDistribution.amounts(popTypeIndex);

                float ratio = resourceGatheringView.employeeAmounts(employeeIndex);
                int neededForThisType = (int) (maxWorkers * ratio);
                int hiredForThisPop = Math.min(popTypeValue, neededForThisType);

                resourceGatheringView.hiredWorkers(popTypeIndex, hiredForThisPop);
            }
        }
    }
}

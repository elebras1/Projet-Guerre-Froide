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

            for (int popIndex = 0; popIndex < popDistribution.populationIdsLength() && popDistribution.populationIds(popIndex) != 0; popIndex++) {
                long popTypeId = popDistribution.populationIds(popIndex);
                int popTypeValue = popDistribution.populationAmounts(popIndex);

                int hiredForThisPop = 0;

                for (int employeeIndex = 0; employeeIndex < resourceGatheringView.employeePopTypeIdsLength() && resourceGatheringView.employeePopTypeIds(employeeIndex) != 0; employeeIndex++) {
                    long requiredPopTypeId = resourceGatheringView.employeePopTypeIds(employeeIndex);

                    if (requiredPopTypeId == popTypeId) {
                        float ratio = resourceGatheringView.employeeAmounts(employeeIndex);
                        int neededForThisType = (int) (maxWorkers * ratio);
                        hiredForThisPop = Math.min(popTypeValue, neededForThisType);
                        break;
                    }
                }

                resourceGatheringView.hiredWorkers(popIndex, hiredForThisPop);
            }
        }
    }
}

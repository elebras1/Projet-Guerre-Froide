package com.populaire.projetguerrefroide.system.economy;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.util.FlecsConstants;
import com.populaire.projetguerrefroide.component.*;

public class ResourceGatheringOperationSizeSystem {

    public ResourceGatheringOperationSizeSystem(World ecsWorld) {
        ecsWorld.system("RGOSizeSystem").kind(FlecsConstants.EcsOnStart).with(Province.class).with(ResourceGathering.class).with(PopulationDistribution.class).multiThreaded().iter(this::process);
    }

    private void process(Iter iter) {
        Field<ResourceGathering> resourceGatheringField = iter.field(ResourceGathering.class, 1);
        Field<PopulationDistribution> populationDistributionField = iter.field(PopulationDistribution.class, 2);
        for (int i = 0; i < iter.count(); i++) {
            ResourceGatheringView resourceGatheringView = resourceGatheringField.getMutView(i);
            PopulationDistributionView populationDistributionView = populationDistributionField.getMutView(i);

            int workforce = resourceGatheringView.workforce();

            long workerPopulationTypeId = resourceGatheringView.employeePopTypeIds(0);

            int workerInProvince = 0;
            for (int j = 0; j < populationDistributionView.populationIdsLength(); j++) {
                if (populationDistributionView.populationIds(j) == workerPopulationTypeId) {
                    workerInProvince = populationDistributionView.populationAmounts(j);
                    break;
                }
            }

            int size = (workerInProvince + workforce - 1) / workforce;
            size = (int) (size * 1.5f);
            resourceGatheringView.size(size);
        }
    }
}

package com.populaire.projetguerrefroide.system.economy;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.util.FlecsConstants;
import com.populaire.projetguerrefroide.component.*;

public class ResourceGatheringOperationSizeSystem {
    private final World ecsWorld;

    public ResourceGatheringOperationSizeSystem(World ecsWorld) {
        this.ecsWorld = ecsWorld;
        ecsWorld.system("RGOSizeSystem").kind(FlecsConstants.EcsOnStart).with(Province.class).with(ResourceGathering.class).with(PopulationDistribution.class).multiThreaded().iter(this::process);
    }

    private void process(Iter iter) {
        Field<ResourceGathering> resourceGatheringField = iter.field(ResourceGathering.class, 1);
        for (int i = 0; i < iter.count(); i++) {
            long provinceId = iter.entity(i);
            EntityView provinceView = this.ecsWorld.obtainEntityView(provinceId);
            ResourceGatheringView resourceGatheringView = resourceGatheringField.getMutView(i);

            EntityView resourceGoodView = this.ecsWorld.obtainEntityView(resourceGatheringView.goodId());
            ResourceProductionView resourceProductionView = resourceGoodView.getMutView(ResourceProduction.class);
            if (resourceProductionView == null) {
                continue;
            }

            EntityView productionTypeView = this.ecsWorld.obtainEntityView(resourceProductionView.productionTypeId());
            ProductionTypeView productionTypeDataView = productionTypeView.getMutView(ProductionType.class);
            if (productionTypeDataView == null || productionTypeDataView.employeeTypes(0) == 0) {
                continue;
            }

            long firstEmployeeEntityId = productionTypeDataView.employeeTypes(0);
            EntityView employeeEntity = this.ecsWorld.obtainEntityView(firstEmployeeEntityId);
            EmployeeTypeView employeeTypeData = employeeEntity.getMutView(EmployeeType.class);
            long workerPopulationTypeId = employeeTypeData.populationTypeId();

            PopulationDistributionView popDistribution = provinceView.getMutView(PopulationDistribution.class);
            int workerInProvince = 0;
            for (int j = 0; j < popDistribution.populationIdsLength(); j++) {
                if (popDistribution.populationIds(j) == workerPopulationTypeId) {
                    workerInProvince = popDistribution.populationAmounts(j);
                    break;
                }
            }

            int size = (workerInProvince + productionTypeDataView.workforce() - 1) / productionTypeDataView.workforce();
            size = (int) (size * 1.5f);
            resourceGatheringView.size(size);
        }
    }
}

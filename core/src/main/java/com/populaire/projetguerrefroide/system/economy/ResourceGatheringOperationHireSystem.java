package com.populaire.projetguerrefroide.system.economy;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.util.FlecsConstants;
import com.populaire.projetguerrefroide.component.*;

public class ResourceGatheringOperationHireSystem {
    private final World ecsWorld;

    public ResourceGatheringOperationHireSystem(World ecsWorld) {
        this.ecsWorld = ecsWorld;
        ecsWorld.system("RGOHireSystem").kind(FlecsConstants.EcsOnUpdate).with(Province.class).with(ResourceGathering.class).with(PopulationDistribution.class).multiThreaded().iter(this::hire);
    }

    public void hire(Iter iter) {
        Field<ResourceGathering> resourceGatheringField = iter.field(ResourceGathering.class, 1);
        Field<PopulationDistribution> populationDistributionField = iter.field(PopulationDistribution.class, 2);
        for (int i = 0; i < iter.count(); i++) {
            ResourceGatheringView resourceGatheringView = resourceGatheringField.getMutView(i);
            PopulationDistributionView popDistribution = populationDistributionField.getMutView(i);
            long resourceGoodId = resourceGatheringView.goodId();

            EntityView resourceGoodView = this.ecsWorld.obtainEntityView(resourceGoodId);
            ResourceProductionView resourceProductionView = resourceGoodView.getMutView(ResourceProduction.class);
            if (resourceProductionView == null) {
                continue;
            }

            EntityView productionTypeView = this.ecsWorld.obtainEntityView(resourceProductionView.productionTypeId());
            ProductionTypeView productionTypeDataView = productionTypeView.getMutView(ProductionType.class);

            int size = resourceGatheringView.size();
            int maxWorkers = size * productionTypeDataView.workforce();

            for (int popIndex = 0; popIndex < popDistribution.populationIdsLength() && popDistribution.populationIds(popIndex) != 0; popIndex++) {
                long popTypeId = popDistribution.populationIds(popIndex);
                int popTypeValue = popDistribution.populationAmounts(popIndex);

                int hiredForThisPop = 0;

                for (int employeeIndex = 0; employeeIndex < productionTypeDataView.employeeTypesLength() && productionTypeDataView.employeeTypes(employeeIndex) != 0; employeeIndex++) {
                    long employeeId = productionTypeDataView.employeeTypes(employeeIndex);
                    EntityView employeeTypeView = this.ecsWorld.obtainEntityView(employeeId);
                    EmployeeTypeView employeeTypeData = employeeTypeView.getMutView(EmployeeType.class);
                    long requiredPopTypeId = employeeTypeData.populationTypeId();

                    if (requiredPopTypeId == popTypeId) {
                        float ratio = employeeTypeData.amount();
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

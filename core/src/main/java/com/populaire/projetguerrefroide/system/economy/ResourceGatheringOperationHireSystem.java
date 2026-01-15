package com.populaire.projetguerrefroide.system.economy;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.util.FlecsConstants;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.service.EconomyService;

public class ResourceGatheringOperationHireSystem {
    private final World ecsWorld;
    private final EconomyService economyService;

    public ResourceGatheringOperationHireSystem(World ecsWorld, EconomyService economyService) {
        this.ecsWorld = ecsWorld;
        this.economyService = economyService;
        ecsWorld.system("RGOHireSystem").kind(FlecsConstants.EcsOnUpdate).with(Province.class).with(ResourceGathering.class).with(PopulationDistribution.class).multiThreaded().iter(this::hire);
    }

    public void hire(Iter iter) {
        for (int i = 0; i < iter.count(); i++) {
            long provinceEntityId = iter.entity(i);
            Entity provinceEntity = ecsWorld.obtainEntity(provinceEntityId);

            long resourceGoodId = iter.fieldLong(ResourceGathering.class, 1, "goodId", i);

            Entity resourceGoodEntity = ecsWorld.obtainEntity(resourceGoodId);
            ResourceProduction resourceProduction = resourceGoodEntity.get(ResourceProduction.class);
            if (resourceProduction == null) {
                continue;
            }

            int size = iter.fieldInt(ResourceGathering.class, 1, "size", i);
            int maxWorkers = economyService.getMaxWorkers(ecsWorld, resourceGoodId, size);

            Entity productionTypeEntity = ecsWorld.obtainEntity(resourceProduction.productionTypeId());
            ProductionType productionTypeData = productionTypeEntity.get(ProductionType.class);

            PopulationDistribution popDistribution = provinceEntity.get(PopulationDistribution.class);
            long[] populationIds = popDistribution.populationIds();
            int[] populationAmounts = popDistribution.populationAmounts();

            int[] hiredWorkers = new int[12];

            for (int popIndex = 0; popIndex < populationIds.length && populationIds[popIndex] != 0; popIndex++) {
                long popTypeId = populationIds[popIndex];
                int popTypeValue = populationAmounts[popIndex];

                int hiredForThisPop = 0;

                for (int employeeIndex = 0; employeeIndex < productionTypeData.employeeTypes().length && productionTypeData.employeeTypes()[employeeIndex] != 0; employeeIndex++) {
                    long employeeId = productionTypeData.employeeTypes()[employeeIndex];
                    EmployeeType employeeType = ecsWorld.obtainEntity(employeeId).get(EmployeeType.class);
                    long requiredPopTypeId = employeeType.populationTypeId();

                    if (requiredPopTypeId == popTypeId) {
                        float ratio = employeeType.amount();
                        int neededForThisType = (int) (maxWorkers * ratio);
                        hiredForThisPop = Math.min(popTypeValue, neededForThisType);
                        break;
                    }
                }

                hiredWorkers[popIndex] = hiredForThisPop;
            }

            iter.setFieldIntArray(ResourceGathering.class, 1, "hiredWorkers", i, hiredWorkers);
        }
    }
}

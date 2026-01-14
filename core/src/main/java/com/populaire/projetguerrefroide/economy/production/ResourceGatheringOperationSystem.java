package com.populaire.projetguerrefroide.economy.production;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.Query;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class ResourceGatheringOperationSystem {

    public void initializeSize(World ecsWorld) {
        try (Query query = ecsWorld.query().with(Province.class).with(ResourceGathering.class).with(PopulationDistribution.class).build()) {
            query.iter(iter -> {
                for (int i = 0; i < iter.count(); i++) {
                    long provinceEntityId = iter.entity(i);
                    Entity provinceEntity = ecsWorld.obtainEntity(provinceEntityId);

                    long resourceGoodId = iter.fieldLong(ResourceGathering.class, 1, "goodId", i);

                    Entity resourceGoodEntity = ecsWorld.obtainEntity(resourceGoodId);
                    ResourceProduction resourceProduction = resourceGoodEntity.get(ResourceProduction.class);
                    if (resourceProduction == null) {
                        continue;
                    }

                    Entity productionTypeEntity = ecsWorld.obtainEntity(resourceProduction.productionTypeId());
                    ProductionType productionTypeData = productionTypeEntity.get(ProductionType.class);
                    if (productionTypeData == null || productionTypeData.employeeTypes()[0] == 0) {
                        continue;
                    }

                    long firstEmployeeEntityId = productionTypeData.employeeTypes()[0];
                    Entity employeeEntity = ecsWorld.obtainEntity(firstEmployeeEntityId);
                    EmployeeType employeeTypeData = employeeEntity.get(EmployeeType.class);
                    long workerPopulationTypeId = employeeTypeData.populationTypeId();

                    PopulationDistribution popDistribution = provinceEntity.get(PopulationDistribution.class);
                    long[] populationIds = popDistribution.populationIds();
                    int[] populationAmounts = popDistribution.populationAmounts();

                    int workerInProvince = 0;
                    for (int j = 0; j < populationIds.length; j++) {
                        if (populationIds[j] == workerPopulationTypeId) {
                            workerInProvince = populationAmounts[j];
                            break;
                        }
                    }

                    int size = (workerInProvince + productionTypeData.workforce() - 1) / productionTypeData.workforce();
                    size = (int) (size * 1.5f);
                    iter.setFieldInt(ResourceGathering.class, 1, "size", i, size);
                }
            });
        }
    }

    public void hire(World ecsWorld) {
        try (Query query = ecsWorld.query().with(Province.class).with(ResourceGathering.class).with(PopulationDistribution.class).build()) {
            query.iter(iter -> {
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
                    int maxWorkers = this.getMaxWorkers(ecsWorld, resourceGoodId, size);

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
            });
        }
    }

    public void produce(World ecsWorld) {
        try (Query query = ecsWorld.query().with(Province.class).with(ResourceGathering.class).build()) {
            query.iter(iter -> {
                for (int i = 0; i < iter.count(); i++) {
                    long provinceEntityId = iter.entity(i);
                    Entity provinceEntity = ecsWorld.obtainEntity(provinceEntityId);

                    long resourceGoodId = iter.fieldLong(ResourceGathering.class, 1, "goodId", i);

                    ResourceGathering currentState = provinceEntity.get(ResourceGathering.class);
                    int resourceGoodSize = currentState.size();

                    Entity resourceGoodEntity = ecsWorld.obtainEntity(resourceGoodId);
                    Good good = resourceGoodEntity.get(Good.class);

                    float baseProduction = resourceGoodSize * good.value();

                    int totalWorkers = 0;
                    int[] hiredWorkers = currentState.hiredWorkers();
                    for (int hiredWorker : hiredWorkers) {
                        totalWorkers += hiredWorker;
                    }

                    int maxWorkers = this.getMaxWorkers(ecsWorld, resourceGoodId, resourceGoodSize);
                    float throughput = maxWorkers > 0 ? (float) totalWorkers / maxWorkers : 0f;
                    float production = baseProduction * throughput;

                    iter.setFieldFloat(ResourceGathering.class, 1, "production", i, production);
                }
            });
        }
    }

    public float getProduction(World ecsWorld, long provinceEntityId) {
        Entity provinceEntity = ecsWorld.obtainEntity(provinceEntityId);
        ResourceGathering state = provinceEntity.get(ResourceGathering.class);
        if (state != null) {
            return state.production();
        }
        return -1f;
    }

    private int getMaxWorkers(World ecsWorld, long resourceGoodId, int resourceGoodSize) {
        Entity resourceGoodEntity = ecsWorld.obtainEntity(resourceGoodId);
        ResourceProduction resourceProduction = resourceGoodEntity.get(ResourceProduction.class);
        Entity productionTypeEntity = ecsWorld.obtainEntity(resourceProduction.productionTypeId());
        ProductionType productionTypeData = productionTypeEntity.get(ProductionType.class);
        return resourceGoodSize * productionTypeData.workforce();
    }
}

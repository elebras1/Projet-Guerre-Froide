package com.populaire.projetguerrefroide.economy.production;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.World;
import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.LongList;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.map.ProvinceStore;

public class ResourceGatheringOperationSystem {

    public void initialiazeSize(World ecsWorld, ProvinceStore provinceStore) {
        IntList provinceResourceGoodsSize = provinceStore.getResourceGoodsSize();

        LongList provinceResourceGoodIds = provinceStore.getResourceGoodIds();

        LongList provincePopulationTypeIds = provinceStore.getPopulationTypeIds();
        IntList provincePopulationTypeStarts = provinceStore.getPopulationTypeStarts();
        IntList provincePopulationTypeCounts = provinceStore.getPopulationTypeCounts();
        IntList provincePopulationTypeValues = provinceStore.getPopulationTypeValues();

        for (int provinceId = 0; provinceId < provinceStore.getIds().size(); provinceId++) {
            long resourceGoodId = provinceResourceGoodIds.get(provinceId);
            if (resourceGoodId == -1) {
                provinceResourceGoodsSize.set(provinceId, -1);
                continue;
            }
            Entity resourceGood = ecsWorld.obtainEntity(resourceGoodId);
            ResourceProduction resourceProduction = resourceGood.get(ResourceProduction.class);
            Entity productionType = ecsWorld.obtainEntity(resourceProduction.productionTypeId());
            ProductionType productionTypeData = productionType.get(ProductionType.class);
            long firstEmployeeEntityId = productionTypeData.employeeTypes()[0];
            Entity employeeEntity = ecsWorld.obtainEntity(firstEmployeeEntityId);
            EmployeeType employeeTypeData = employeeEntity.get(EmployeeType.class);
            long workerPopulationTypeId = employeeTypeData.populationTypeId();


            int populationTypeStart = provincePopulationTypeStarts.get(provinceId);
            int populationTypeCount = provincePopulationTypeCounts.get(provinceId);
            int populationTypeEnd = populationTypeStart + populationTypeCount;

            int workerInProvince = 0;
            for (int populationTypeIndex = populationTypeStart; populationTypeIndex < populationTypeEnd; populationTypeIndex++) {
                long populationTypeId = provincePopulationTypeIds.get(populationTypeIndex);
                if (populationTypeId == workerPopulationTypeId) {
                    workerInProvince = provincePopulationTypeValues.get(populationTypeIndex);
                    break;
                }
            }

            int size = (workerInProvince + productionTypeData.workforce() - 1) / productionTypeData.workforce();
            size = (int)(size * 1.5f);

            provinceResourceGoodsSize.set(provinceId, size);
        }
    }

    public void hire(World ecsWorld, ProvinceStore provinceStore) {
        IntList provinceResourceGoodsPopulationAmountValues = provinceStore.getResourceGoodsPopulationAmountValues();
        LongList provinceResourceGoodIds = provinceStore.getResourceGoodIds();

        LongList provincePopulationTypeIds = provinceStore.getPopulationTypeIds();
        IntList provincePopulationTypeStarts = provinceStore.getPopulationTypeStarts();
        IntList provincePopulationTypeCounts = provinceStore.getPopulationTypeCounts();
        IntList provincePopulationTypeValues = provinceStore.getPopulationTypeValues();

        for (int provinceId = 0; provinceId < provinceStore.getIds().size(); provinceId++) {
            long resourceGoodId = provinceResourceGoodIds.get(provinceId);

            if (resourceGoodId == -1) {
                continue;
            }
            Entity resourceGood = ecsWorld.obtainEntity(resourceGoodId);
            ResourceProduction resourceProduction = resourceGood.get(ResourceProduction.class);
            int maxWorkers = getMaxWorkers(ecsWorld, provinceStore, provinceId);

            Entity productionType = ecsWorld.obtainEntity(resourceProduction.productionTypeId());
            ProductionType productionTypeData = productionType.get(ProductionType.class);

            int populationTypeStart = provincePopulationTypeStarts.get(provinceId);
            int populationTypeCount = provincePopulationTypeCounts.get(provinceId);

            for (int popIndex = populationTypeStart; popIndex < populationTypeStart + populationTypeCount; popIndex++) {
                long popTypeId = provincePopulationTypeIds.get(popIndex);
                int popTypeValue = provincePopulationTypeValues.get(popIndex);

                int hiredForThisPop = 0;

                for (int employeeIndex = 0; employeeIndex < productionTypeData.employeeTypes().length && productionTypeData.employeeTypes()[employeeIndex] != 0; employeeIndex++) {
                    long employeeId = productionTypeData.employeeTypes()[employeeIndex];
                    EmployeeType employeeType = ecsWorld.obtainEntity(employeeId).get(EmployeeType.class);
                    long requiredPopTypeId = employeeType.populationTypeId();

                    if (requiredPopTypeId == popTypeId) {
                        float ratio = employeeType.amount();
                        int neededForThisType = (int)(maxWorkers * ratio);
                        hiredForThisPop = Math.min(popTypeValue, neededForThisType);
                        break;
                    }
                }

                provinceResourceGoodsPopulationAmountValues.set(popIndex, hiredForThisPop);
            }
        }
    }

    public void produce(World ecsWorld, ProvinceStore provinceStore) {
        FloatList provinceResourceGoodsProductions = provinceStore.getResourceGoodsProduction();

        IntList provinceResourceGoodsSize = provinceStore.getResourceGoodsSize();
        LongList provinceResourceGoodIds = provinceStore.getResourceGoodIds();

        IntList provincePopulationTypeStarts = provinceStore.getPopulationTypeStarts();
        IntList provincePopulationTypeCounts = provinceStore.getPopulationTypeCounts();

        for (int provinceId = 0; provinceId < provinceStore.getIds().size(); provinceId++) {
            int provinceResourceGoodSize = provinceResourceGoodsSize.get(provinceId);
            long resourceGoodId = provinceResourceGoodIds.get(provinceId);
            if (resourceGoodId == -1) {
                provinceResourceGoodsProductions.set(provinceId, -1);
                continue;
            }

            Entity resourceGood = ecsWorld.obtainEntity(resourceGoodId);
            Good good = resourceGood.get(Good.class);

            float baseProduction = provinceResourceGoodSize * good.value();

            int totalWorkers = 0;
            int populationTypeStart = provincePopulationTypeStarts.get(provinceId);
            int populationTypeCount = provincePopulationTypeCounts.get(provinceId);
            for (int popIndex = populationTypeStart; popIndex < populationTypeStart + populationTypeCount; popIndex++) {
                totalWorkers += provinceStore.getResourceGoodsPopulationAmountValues().get(popIndex);
            }

            float throughput = (float) totalWorkers / this.getMaxWorkers(ecsWorld, provinceStore, provinceId);
            float production = baseProduction * throughput;

            provinceResourceGoodsProductions.set(provinceId, production);
        }
    }

    private int getMaxWorkers(World ecsWorld, ProvinceStore provinceStore, int provinceId) {
        int resourceGoodSize = provinceStore.getResourceGoodsSize().get(provinceId);
        long resourceGoodId = provinceStore.getResourceGoodIds().get(provinceId);
        Entity resourceGood = ecsWorld.obtainEntity(resourceGoodId);
        ResourceProduction resourceProduction = resourceGood.get(ResourceProduction.class);
        Entity productionType = ecsWorld.obtainEntity(resourceProduction.productionTypeId());
        ProductionType productionTypeData = productionType.get(ProductionType.class);
        return resourceGoodSize * productionTypeData.workforce();
    }
}

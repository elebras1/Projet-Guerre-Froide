package com.populaire.projetguerrefroide.economy.production;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.World;
import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.LongList;
import com.populaire.projetguerrefroide.component.Good;
import com.populaire.projetguerrefroide.component.ResourceProduction;
import com.populaire.projetguerrefroide.economy.building.EmployeeStore;
import com.populaire.projetguerrefroide.economy.building.ProductionTypeStore;
import com.populaire.projetguerrefroide.map.ProvinceStore;

public class ResourceGatheringOperationSystem {

    public void initialiazeSize(World ecsWorld, ProvinceStore provinceStore, ProductionTypeStore productionTypeStore, EmployeeStore employeeStore) {
        IntList provinceResourceGoodsSize = provinceStore.getResourceGoodsSize();

        LongList provinceResourceGoodIds = provinceStore.getResourceGoodIds();

        IntList productionTypeWorkforces = productionTypeStore.getWorkforces();
        IntList productionTypeEmployeeStarts = productionTypeStore.getEmployeeStarts();
        IntList productionTypeEmployeeIds = productionTypeStore.getEmployeeIds();

        LongList employeePopulationTypeIds = employeeStore.getPopulationTypeIds();

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
            int workforce = productionTypeWorkforces.get(resourceProduction.productionTypeId());

            int employeeStart = productionTypeEmployeeStarts.get(resourceProduction.productionTypeId());
            int employeeId = productionTypeEmployeeIds.get(employeeStart);
            long workerPopulationTypeId = employeePopulationTypeIds.get(employeeId);

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

            int size = (workerInProvince + workforce - 1) / workforce;
            size = (int)(size * 1.5f);

            provinceResourceGoodsSize.set(provinceId, size);
        }
    }

    public void hire(World ecsWorld, ProvinceStore provinceStore, ProductionTypeStore productionTypeStore, EmployeeStore employeeStore) {
        IntList provinceResourceGoodsPopulationAmountValues = provinceStore.getResourceGoodsPopulationAmountValues();
        LongList provinceResourceGoodIds = provinceStore.getResourceGoodIds();

        IntList productionTypeEmployeeStarts = productionTypeStore.getEmployeeStarts();
        IntList productionTypeEmployeeCounts = productionTypeStore.getEmployeeCounts();
        IntList productionTypeEmployeeIds = productionTypeStore.getEmployeeIds();

        LongList employeePopulationTypeIds = employeeStore.getPopulationTypeIds();
        FloatList employeeAmounts = employeeStore.getAmounts();

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
            int maxWorkers = getMaxWorkers(ecsWorld, provinceStore, productionTypeStore, provinceId);

            int employeeStart = productionTypeEmployeeStarts.get(resourceProduction.productionTypeId());
            int employeeCount = productionTypeEmployeeCounts.get(resourceProduction.productionTypeId());

            int populationTypeStart = provincePopulationTypeStarts.get(provinceId);
            int populationTypeCount = provincePopulationTypeCounts.get(provinceId);

            for (int popIndex = populationTypeStart; popIndex < populationTypeStart + populationTypeCount; popIndex++) {
                long popTypeId = provincePopulationTypeIds.get(popIndex);
                int popTypeValue = provincePopulationTypeValues.get(popIndex);

                int hiredForThisPop = 0;

                for (int employeeIndex = employeeStart; employeeIndex < employeeStart + employeeCount; employeeIndex++) {
                    int employeeId = productionTypeEmployeeIds.get(employeeIndex);
                    long requiredPopTypeId = employeePopulationTypeIds.get(employeeId);

                    if (requiredPopTypeId == popTypeId) {
                        float ratio = employeeAmounts.get(employeeId);
                        int neededForThisType = (int)(maxWorkers * ratio);
                        hiredForThisPop = Math.min(popTypeValue, neededForThisType);
                        break;
                    }
                }

                provinceResourceGoodsPopulationAmountValues.set(popIndex, hiredForThisPop);
            }
        }
    }

    public void produce(World ecsWorld, ProvinceStore provinceStore, ProductionTypeStore productionTypeStore) {
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

            float throughput = (float) totalWorkers / this.getMaxWorkers(ecsWorld, provinceStore, productionTypeStore, provinceId);
            float production = baseProduction * throughput;

            provinceResourceGoodsProductions.set(provinceId, production);
        }
    }

    private int getMaxWorkers(World ecsWorld, ProvinceStore provinceStore, ProductionTypeStore productionTypeStore, int provinceId) {
        int resourceGoodSize = provinceStore.getResourceGoodsSize().get(provinceId);
        long resourceGoodId = provinceStore.getResourceGoodIds().get(provinceId);
        Entity resourceGood = ecsWorld.obtainEntity(resourceGoodId);
        ResourceProduction resourceProduction = resourceGood.get(ResourceProduction.class);
        int workforce = productionTypeStore.getWorkforces().get(resourceProduction.productionTypeId());
        return resourceGoodSize * workforce;
    }
}

package com.populaire.projetguerrefroide.economy.production;

import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.IntList;
import com.populaire.projetguerrefroide.economy.building.EmployeeStore;
import com.populaire.projetguerrefroide.economy.building.ProductionTypeStore;
import com.populaire.projetguerrefroide.economy.good.GoodStore;
import com.populaire.projetguerrefroide.map.ProvinceStore;

public class ResourceGatheringOperationSystem {

    public void initialiazeSize(ProvinceStore provinceStore, GoodStore goodStore, ProductionTypeStore productionTypeStore, EmployeeStore employeeStore) {
        IntList provinceResourceGoodsSize = provinceStore.getResourceGoodsSize();

        IntList provinceResourceGoodIds = provinceStore.getResourceGoodIds();
        IntList goodProductionTypeIds = goodStore.getProductionTypeIds();

        IntList productionTypeWorkforces = productionTypeStore.getWorkforces();
        IntList productionTypeEmployeeStarts = productionTypeStore.getEmployeeStarts();
        IntList productionTypeEmployeeIds = productionTypeStore.getEmployeeIds();

        IntList employeePopulationTypeIds = employeeStore.getPopulationTypeIds();

        IntList provincePopulationTypeIds = provinceStore.getPopulationTypeIds();
        IntList provincePopulationTypeStarts = provinceStore.getPopulationTypeStarts();
        IntList provincePopulationTypeCounts = provinceStore.getPopulationTypeCounts();
        IntList provincePopulationTypeValues = provinceStore.getPopulationTypeValues();

        for (int provinceId = 0; provinceId < provinceStore.getIds().size(); provinceId++) {
            int resourceGoodId = provinceResourceGoodIds.get(provinceId);
            if (resourceGoodId == -1) {
                provinceResourceGoodsSize.set(provinceId, -1);
                continue;
            }

            int productionTypeId = goodProductionTypeIds.get(resourceGoodId);
            int workforce = productionTypeWorkforces.get(productionTypeId);

            int employeeStart = productionTypeEmployeeStarts.get(productionTypeId);
            int employeeId = productionTypeEmployeeIds.get(employeeStart);
            int workerPopulationTypeId = employeePopulationTypeIds.get(employeeId);

            int populationTypeStart = provincePopulationTypeStarts.get(provinceId);
            int populationTypeCount = provincePopulationTypeCounts.get(provinceId);
            int populationTypeEnd = populationTypeStart + populationTypeCount;

            int workerInProvince = 0;
            for (int populationTypeIndex = populationTypeStart; populationTypeIndex < populationTypeEnd; populationTypeIndex++) {
                int populationTypeId = provincePopulationTypeIds.get(populationTypeIndex);
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

    public void hire(ProvinceStore provinceStore, GoodStore goodStore, ProductionTypeStore productionTypeStore, EmployeeStore employeeStore) {
    }

    public void produce(ProvinceStore provinceStore, GoodStore goodStore, ProductionTypeStore productionTypeStore, EmployeeStore employeeStore) {
        FloatList provinceResourceGoodsProductions = provinceStore.getResourceGoodsProduction();

        IntList provinceResourceGoodsSize = provinceStore.getResourceGoodsSize();
        IntList provinceResourceGoodIds = provinceStore.getResourceGoodIds();
        FloatList goodProductionValues = goodStore.getValues();

        for (int provinceId = 0; provinceId < provinceStore.getIds().size(); provinceId++) {
            int provinceResourceGoodSize = provinceResourceGoodsSize.get(provinceId);
            int resourceGoodId = provinceResourceGoodIds.get(provinceId);
            if (resourceGoodId == -1) {
                provinceResourceGoodsProductions.set(provinceId, -1);
                continue;
            }

            float goodProductionValue = goodProductionValues.get(resourceGoodId);

            float baseProduction = provinceResourceGoodSize * goodProductionValue;
            provinceResourceGoodsProductions.set(provinceId, baseProduction);
        }
    }

    private int getMaxWorkers(ProvinceStore provinceStore, ProductionTypeStore productionTypeStore, GoodStore goodStore, int provinceId) {
        int resourceGoodSize = provinceStore.getResourceGoodsSize().get(provinceId);
        int resourceGoodId = provinceStore.getResourceGoodIds().get(provinceId);
        int productionTypeId = goodStore.getProductionTypeIds().get(resourceGoodId);
        int workforce = productionTypeStore.getWorkforces().get(productionTypeId);
        return resourceGoodSize * workforce;
    }
}

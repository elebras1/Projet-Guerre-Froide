package com.populaire.projetguerrefroide.economy;

import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.ObjectIntMap;
import com.populaire.projetguerrefroide.economy.building.BuildingStore;
import com.populaire.projetguerrefroide.economy.building.EmployeeStore;
import com.populaire.projetguerrefroide.economy.building.ProductionTypeStore;
import com.populaire.projetguerrefroide.economy.good.GoodStore;
import com.populaire.projetguerrefroide.economy.population.PopulationTypeStore;
import com.populaire.projetguerrefroide.map.Country;
import com.populaire.projetguerrefroide.map.LandProvince;
import com.populaire.projetguerrefroide.map.ProvinceStore;

public class Economy {
    private final short maxProvinceId;
    private final BuildingStore buildingStore;
    private final GoodStore goodStore;
    private final ProductionTypeStore productionTypeStore;
    private final EmployeeStore employeeStore;
    private final PopulationTypeStore populationTypeStore;
    private final ObjectIntMap<String> goodIds;
    private final ObjectIntMap<String> buildingIds;
    private final ObjectIntMap<String> populationTypeIds;
    private final ObjectIntMap<String> productionTypeIds;

    public Economy(short maxProvinceId, BuildingStore buildingStore, GoodStore goodStore, ProductionTypeStore productionTypeStore, EmployeeStore employeeStore, PopulationTypeStore populationTypeStore, ObjectIntMap<String> goodIds, ObjectIntMap<String> buildingIds, ObjectIntMap<String> populationTypeIds, ObjectIntMap<String> productionTypeIds) {
        this.maxProvinceId = maxProvinceId;
        this.buildingStore = buildingStore;
        this.goodStore = goodStore;
        this.productionTypeStore = productionTypeStore;
        this.employeeStore = employeeStore;
        this.populationTypeStore = populationTypeStore;
        this.goodIds = goodIds;
        this.buildingIds = buildingIds;
        this.populationTypeIds = populationTypeIds;
        this.productionTypeIds = productionTypeIds;
    }

    public short getMaxProvinceId() {
        return this.maxProvinceId;
    }

    public BuildingStore getBuildingStore() {
        return this.buildingStore;
    }

    public GoodStore getGoodStore() {
        return this.goodStore;
    }

    public ProductionTypeStore getProductionTypeStore() {
        return this.productionTypeStore;
    }

    public EmployeeStore getEmployeeStore() {
        return this.employeeStore;
    }

    public ObjectIntMap<String> getGoodIds() {
        return this.goodIds;
    }

    public ObjectIntMap<String> getBuildingIds() {
        return this.buildingIds;
    }

    public ObjectIntMap<String> getPopulationTypeIds() {
        return this.populationTypeIds;
    }

    public ObjectIntMap<String> getProductionTypeIds() {
        return this.productionTypeIds;
    }

    public int getPopulationAmount(ProvinceStore provinceStore, Country country) {
        int population = 0;
        for(LandProvince province : country.getProvinces()) {
            int provinceId = province.getId();
            population += provinceStore.getPopulationAmount(provinceId);
        }
        return population;
    }

    public void setResourceGoodsSize(ProvinceStore provinceStore) {
        int[] provinceResourceGoodsSize = provinceStore.getResourceGoodsSize().items;

        IntList provinceResourceGoodIds = provinceStore.getResourceGoodIds();
        IntList goodProductionTypeIds = this.goodStore.getProductionTypeIds();

        IntList productionTypeWorkforces = this.productionTypeStore.getWorkforces();
        IntList productionTypeEmployeeStarts = this.productionTypeStore.getEmployeeStarts();
        IntList productionTypeEmployeeIds = this.productionTypeStore.getEmployeeIds();

        IntList employeePopulationTypeIds = this.employeeStore.getPopulationTypeIds();

        IntList provincePopulationTypeIds = provinceStore.getPopulationTypeIds();
        IntList provincePopulationTypeStarts = provinceStore.getPopulationTypeStarts();
        IntList provincePopulationTypeCounts = provinceStore.getPopulationTypeCounts();
        IntList provincePopulationTypeValues = provinceStore.getPopulationTypeValues();

        for (int provinceId = 1; provinceId < this.maxProvinceId; provinceId++) {
            int resourceGoodId = provinceResourceGoodIds.get(provinceId);
            if (resourceGoodId == -1) {
                provinceResourceGoodsSize[provinceId] = -1;
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

            provinceResourceGoodsSize[provinceId] = size;
        }
    }

}

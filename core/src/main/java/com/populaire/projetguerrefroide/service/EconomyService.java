package com.populaire.projetguerrefroide.service;

import com.github.tommyettinger.ds.*;
import com.populaire.projetguerrefroide.dto.RegionsBuildingsDto;
import com.populaire.projetguerrefroide.economy.building.BuildingStore;
import com.populaire.projetguerrefroide.economy.production.ResourceGatheringOperationSystem;
import com.populaire.projetguerrefroide.map.*;
import com.populaire.projetguerrefroide.ui.view.SortType;

import java.util.List;

public class EconomyService {
    private final WorldContext worldContext;
    private ResourceGatheringOperationSystem rgoSystem;

    public EconomyService(WorldContext worldContext) {
        this.worldContext = worldContext;
        this.rgoSystem = new ResourceGatheringOperationSystem();
    }

    public void initialize() {
        this.rgoSystem.initialiazeSize(this.worldContext.getProvinceStore(), this.worldContext.getGoodStore(), this.worldContext.getProductionTypeStore(), this.worldContext.getEmployeeStore());
    }

    public void hire() {
        this.rgoSystem.hire(this.worldContext.getProvinceStore(), this.worldContext.getGoodStore(), this.worldContext.getProductionTypeStore(), this.worldContext.getEmployeeStore());
    }

    public void produce() {
        this.rgoSystem.produce(this.worldContext.getProvinceStore(), this.worldContext.getGoodStore(), this.worldContext.getProductionTypeStore());
    }

    public RegionsBuildingsDto prepareRegionsBuildingsDto() {
        /*RegionStore regionStore = this.worldContext.getRegionStore();
        BuildingStore buildingStore = this.worldContext.getBuildingStore();
        List<String> regionIds = new ObjectList<>();
        for(Region region : this.worldContext.getPlayerCountryId().getRegions()) {
            regionIds.add(region.getId());
        }
        IntList regionInternalIds = new IntList(regionStore.getRegionIds().size());
        for(int regionId = 0; regionId < regionIds.size(); regionId++) {
            regionInternalIds.add(regionId);
        }
        ObjectIntMap<String> regionIdLookup = new ObjectIntMap<>(regionStore.getRegionIds());
        IntList buildingIds = new IntList(regionStore.getBuildingIds());
        IntList buildingValues = new IntList(regionStore.getBuildingValues());
        IntList buildingStarts = new IntList(regionStore.getBuildingStarts());
        IntList buildingCounts = new IntList(regionStore.getBuildingCounts());
        List<String> buildingNames = new ObjectList<>(buildingStore.getNames());
        ByteList buildingTypes = new ByteList(buildingStore.getTypes());
        ByteList buildingMaxLevels = new ByteList(buildingStore.getMaxLevels());
        ByteList developpementIndexValues = new ByteList(regionStore.getBuildingIds().size());
        developpementIndexValues.setSize(regionStore.getBuildingIds().size());
        IntList populationsAmount = this.getPopulationsAmount(this.worldContext.getCountries(), regionStore, this.worldContext.getProvinceStore());
        IntList buildingWorkersAmount = this.getBuildingWorkersAmount(regionStore);
        IntList workersAmount = this.getWorkersAmount(this.worldContext.getCountries(), this.worldContext.getRegionStore(), this.worldContext.getProvinceStore());
        ByteList buildingWorkersRatio = this.getBuildingWorkersRatio(workersAmount, buildingWorkersAmount);
        FloatList buildingProductionValues = new FloatList(regionStore.getBuildingProductionValues());

        return new RegionsBuildingsDto(regionIds, regionInternalIds, regionIdLookup, buildingIds, buildingValues, buildingStarts, buildingCounts, buildingNames, buildingTypes, buildingMaxLevels, developpementIndexValues, populationsAmount, buildingWorkersAmount, buildingWorkersRatio, buildingProductionValues);*/
        return null;
    }

    public RegionsBuildingsDto prepareRegionsBuildingsDtoSorted(SortType sortType) {
        return null;
    }

    public float getResourceGoodsProduction(short provinceId) {
        int provinceIndex = this.worldContext.getProvinceStore().getIndexById().get(provinceId);
        return this.worldContext.getProvinceStore().getResourceGoodsProduction().get(provinceIndex);
    }

    private IntList getPopulationsAmount(RegionStore regionStore, ProvinceStore provinceStore) {
        IntList populationsAmount = new IntList();
        /*populationsAmount.setSize(this.worldContext.getRegionStore().getRegionIds().size());
        for(Country country : countries) {
            for (Region region : country.getRegions()) {
                int population = 0;
                for (LandProvince province : region.getProvinceIds()) {
                    int provinceId = provinceStore.getIndexById().get(province.getId());
                    population += this.worldContext.getProvinceStore().getPopulationAmount(provinceId);
                }
                int regionId = regionStore.getRegionIds().get(region.getId());
                populationsAmount.set(regionId, population);
            }
        }*/

        return populationsAmount;
    }

    private IntList getWorkersAmount(RegionStore regionStore, ProvinceStore provinceStore) {
        IntList workersAmount = new IntList(regionStore.getRegionIds().size());
        /*workersAmount.setSize(regionStore.getRegionIds().size());
        for(Country country : countries) {
            for(Region region : country.getRegions()) {
                int workers = 0;
                for(LandProvince province : region.getProvinceIds()) {
                    workers += this.getAmountAdults(province, provinceStore);
                }
                int regionId = regionStore.getRegionIds().get(region.getId());
                workersAmount.set(regionId, workers);
            }
        }*/

        return workersAmount;
    }

    private IntList getBuildingWorkersAmount(RegionStore regionStore) {
        IntList buildingWorkersAmount = new IntList(regionStore.getRegionIds().size());
        buildingWorkersAmount.setSize(regionStore.getRegionIds().size());
        for(int regionId = 0; regionId < regionStore.getRegionIds().size(); regionId++) {
            int buildingStart = regionStore.getBuildingStarts().get(regionId);
            int buildingCount = regionStore.getBuildingCounts().get(regionId);
            int amountWorkers = 0;
            for(int buildingId = buildingStart; buildingId < buildingStart + buildingCount; buildingId++) {
                amountWorkers += regionStore.getBuildingWorkersAmountValues().get(buildingId);
            }

            buildingWorkersAmount.set(regionId, amountWorkers);
        }

        return buildingWorkersAmount;
    }

    private ByteList getBuildingWorkersRatio(IntList workersAmount, IntList buildingWorkersAmount) {
        ByteList buildingWorkersRatio = new ByteList(workersAmount.size());
        buildingWorkersRatio.setSize(workersAmount.size());

        for(int regionId = 0; regionId < workersAmount.size(); regionId++) {
            int totalWorkers = workersAmount.get(regionId);
            int buildingWorkers = buildingWorkersAmount.get(regionId);
            if(totalWorkers == 0) {
                buildingWorkersRatio.set(regionId, (byte)0);
            } else {
                float ratio = (float)buildingWorkers / (float)totalWorkers;
                byte ratioByte = (byte)(ratio * 100);
                buildingWorkersRatio.set(regionId, ratioByte);
            }
        }

        return buildingWorkersRatio;
    }

    private int getAmountAdults(long provinceId, ProvinceStore provinceStore) {
        /*int provinceId = province.getId();
        int provinceIndex = provinceStore.getIndexById().get(provinceId);
        return provinceStore.getAmountAdults().get(provinceIndex);*/
        return 0;
    }
}

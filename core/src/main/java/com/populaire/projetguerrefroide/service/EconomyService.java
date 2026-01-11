package com.populaire.projetguerrefroide.service;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.Query;
import com.github.elebras1.flecs.World;
import com.github.tommyettinger.ds.*;
import com.populaire.projetguerrefroide.component.GeoHierarchy;
import com.populaire.projetguerrefroide.component.Province;
import com.populaire.projetguerrefroide.dto.RegionsBuildingsDto;
import com.populaire.projetguerrefroide.economy.building.BuildingStore;
import com.populaire.projetguerrefroide.economy.production.ResourceGatheringOperationSystem;
import com.populaire.projetguerrefroide.map.*;
import com.populaire.projetguerrefroide.ui.view.SortType;
import com.populaire.projetguerrefroide.util.EcsConstants;

import java.util.List;
import java.util.Set;

public class EconomyService {
    private final GameContext gameContext;
    private final WorldContext worldContext;
    private ResourceGatheringOperationSystem rgoSystem;

    public EconomyService(GameContext gameContext, WorldContext worldContext) {
        this.gameContext = gameContext;
        this.worldContext = worldContext;
        this.rgoSystem = new ResourceGatheringOperationSystem();
    }

    public void initialize() {
        this.rgoSystem.initialiazeSize(this.gameContext.getEcsWorld(), this.worldContext.getProvinceStore());
    }

    public void hire() {
        this.rgoSystem.hire(this.gameContext.getEcsWorld(), this.worldContext.getProvinceStore());
    }

    public void produce() {
        this.rgoSystem.produce(this.gameContext.getEcsWorld(), this.worldContext.getProvinceStore());
    }

    public RegionsBuildingsDto prepareRegionsBuildingsDto() {
        World ecsWorld = this.gameContext.getEcsWorld();
        EcsConstants ecsConstants = this.gameContext.getEcsConstants();
        RegionStore regionStore = this.worldContext.getRegionStore();
        BuildingStore buildingStore = this.worldContext.getBuildingStore();
        Set<String> regionIds = new ObjectSet<>();
        try (Query query = ecsWorld.query().with(Province.class).build()) {
            query.iter(iter -> {
                for (int i = 0; i < iter.count(); i++) {
                    Entity province = ecsWorld.obtainEntity(iter.entity(i));
                    long ownerId = iter.fieldLong(Province.class, 0, "ownerId", i);
                    if (ownerId == this.worldContext.getPlayerCountryId()) {
                        GeoHierarchy geoHierarchy = province.get(GeoHierarchy.class);
                        long regionId = geoHierarchy.regionId();
                        String regionNameId = ecsWorld.obtainEntity(regionId).getName();
                        regionIds.add(regionNameId);
                    }
                }
            });
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
        IntList populationsAmount = this.getPopulationsAmount(regionStore, this.worldContext.getProvinceStore());
        IntList buildingWorkersAmount = this.getBuildingWorkersAmount(regionStore);
        IntList workersAmount = this.getWorkersAmount(this.worldContext.getRegionStore(), this.worldContext.getProvinceStore());
        ByteList buildingWorkersRatio = this.getBuildingWorkersRatio(workersAmount, buildingWorkersAmount);
        FloatList buildingProductionValues = new FloatList(regionStore.getBuildingProductionValues());

        return new RegionsBuildingsDto(regionIds, regionInternalIds, regionIdLookup, buildingIds, buildingValues, buildingStarts, buildingCounts, buildingNames, buildingTypes, buildingMaxLevels, developpementIndexValues, populationsAmount, buildingWorkersAmount, buildingWorkersRatio, buildingProductionValues);
    }

    public RegionsBuildingsDto prepareRegionsBuildingsDtoSorted(SortType sortType) {
        return null;
    }

    public float getResourceGoodsProduction(int provinceId) {
        int provinceIndex = this.worldContext.getProvinceStore().getIndexById().get(provinceId);
        return this.worldContext.getProvinceStore().getResourceGoodsProduction().get(provinceIndex);
    }

    private IntList getPopulationsAmount(RegionStore regionStore, ProvinceStore provinceStore) {
        World ecsWorld = this.gameContext.getEcsWorld();
        IntList populationsAmount = new IntList();
        populationsAmount.setSize(regionStore.getRegionIds().size());

        try (Query query = ecsWorld.query().with(Province.class).with(GeoHierarchy.class).build()) {
            query.iter(iter -> {
                for(int i = 0; i < iter.count(); i++) {
                    Entity provinceEntity = ecsWorld.obtainEntity(iter.entityId(i));
                    long regionId = iter.fieldLong(GeoHierarchy.class, 1, "regionId", i);
                    String regionNameId = ecsWorld.obtainEntity(regionId).getName();
                    int regionIndex = regionStore.getRegionIds().get(regionNameId);
                    int provinceNameId = Integer.parseInt(provinceEntity.getName());
                    int provinceIndex = provinceStore.getIndexById().get(provinceNameId);
                    int population = provinceStore.getPopulationAmount(provinceIndex);
                    int currentPopulation = populationsAmount.get(regionIndex);
                    populationsAmount.set(regionIndex, currentPopulation + population);
                }
            });
        }

        return populationsAmount;
    }

    private IntList getWorkersAmount(RegionStore regionStore, ProvinceStore provinceStore) {
        World ecsWorld = this.gameContext.getEcsWorld();
        IntList workersAmount = new IntList(regionStore.getRegionIds().size());
        workersAmount.setSize(regionStore.getRegionIds().size());

        try (Query query = ecsWorld.query().with(Province.class).with(GeoHierarchy.class).build()) {
            query.iter(iter -> {
                for(int i = 0; i < iter.count(); i++) {
                    Entity provinceEntity = ecsWorld.obtainEntity(iter.entityId(i));
                    long regionId = iter.fieldLong(GeoHierarchy.class, 1, "regionId", i);
                    String regionNameId = ecsWorld.obtainEntity(regionId).getName();
                    int regionIndex = regionStore.getRegionIds().get(regionNameId);
                    int adults = this.getAmountAdults(provinceEntity.id(), provinceStore);
                    int currentWorkers = workersAmount.get(regionIndex);
                    workersAmount.set(regionIndex, currentWorkers + adults);
                }
            });
        }

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
        int provinceNameId = Integer.parseInt(this.gameContext.getEcsWorld().obtainEntity(provinceId).getName());
        int provinceIndex = provinceStore.getIndexById().get(provinceNameId);
        return provinceStore.getAmountAdults().get(provinceIndex);
    }
}

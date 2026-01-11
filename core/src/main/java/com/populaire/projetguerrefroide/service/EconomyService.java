package com.populaire.projetguerrefroide.service;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.Query;
import com.github.elebras1.flecs.World;
import com.github.tommyettinger.ds.*;
import com.populaire.projetguerrefroide.component.GeoHierarchy;
import com.populaire.projetguerrefroide.component.Province;
import com.populaire.projetguerrefroide.dto.BuildingDto;
import com.populaire.projetguerrefroide.dto.RegionDto;
import com.populaire.projetguerrefroide.dto.RegionsBuildingsDto;
import com.populaire.projetguerrefroide.economy.production.ResourceGatheringOperationSystem;
import com.populaire.projetguerrefroide.map.*;
import com.populaire.projetguerrefroide.ui.view.SortType;

import java.util.List;
import java.util.Map;
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
    }

    public RegionsBuildingsDto prepareRegionsBuildingsDtoSorted(SortType sortType) {
        return null;
    }

    public float getResourceGoodsProduction(int provinceId) {
        int provinceIndex = this.worldContext.getProvinceStore().getIndexById().get(provinceId);
        return this.worldContext.getProvinceStore().getResourceGoodsProduction().get(provinceIndex);
    }

    private IntList prepareRegionsDto(ProvinceStore provinceStore, Map<RegionDto, List<BuildingDto>> regionsBuildings) {
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

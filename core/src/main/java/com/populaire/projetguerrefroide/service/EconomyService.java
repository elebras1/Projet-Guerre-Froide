package com.populaire.projetguerrefroide.service;

import com.github.tommyettinger.ds.ByteList;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.ObjectIntMap;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.dto.RegionsBuildingsDto;
import com.populaire.projetguerrefroide.economy.building.BuildingStore;
import com.populaire.projetguerrefroide.economy.production.ResourceGatheringOperationSystem;
import com.populaire.projetguerrefroide.map.Region;
import com.populaire.projetguerrefroide.map.RegionStore;
import com.populaire.projetguerrefroide.map.WorldContext;

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
        RegionStore regionStore = this.worldContext.getRegionStore();
        BuildingStore buildingStore = this.worldContext.getBuildingStore();
        List<String> regionIds = new ObjectList<>();
        for(Region region : this.worldContext.getPlayerCountry().getRegions()) {
            regionIds.add(region.getId());
        }
        ObjectIntMap<String> regionIdLookup = new ObjectIntMap<>(regionStore.getRegionIds());
        IntList buildingIds = new IntList(regionStore.getBuildingIds());
        IntList buildingValues = new IntList(regionStore.getBuildingValues());
        IntList buildingStarts = new IntList(regionStore.getBuildingStarts());
        IntList buildingCounts = new IntList(regionStore.getBuildingCounts());
        List<String> buildingNames = new ObjectList<>(buildingStore.getNames());
        ByteList buildingTypes = new ByteList(buildingStore.getTypes());

        return new RegionsBuildingsDto(regionIds, regionIdLookup, buildingIds, buildingValues, buildingStarts, buildingCounts, buildingNames, buildingTypes);
    }

    public float getResourceGoodsProduction(short provinceId) {
        int provinceIndex = this.worldContext.getProvinceStore().getIndexById().get(provinceId);
        return this.worldContext.getProvinceStore().getResourceGoodsProduction().get(provinceIndex);
    }
}

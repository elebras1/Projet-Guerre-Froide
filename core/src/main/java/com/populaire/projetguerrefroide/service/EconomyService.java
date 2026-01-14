package com.populaire.projetguerrefroide.service;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.Query;
import com.github.elebras1.flecs.World;
import com.github.tommyettinger.ds.*;
import com.populaire.projetguerrefroide.component.Building;
import com.populaire.projetguerrefroide.component.EconomyBuilding;
import com.populaire.projetguerrefroide.component.GeoHierarchy;
import com.populaire.projetguerrefroide.component.Province;
import com.populaire.projetguerrefroide.dto.BuildingDto;
import com.populaire.projetguerrefroide.dto.RegionDto;
import com.populaire.projetguerrefroide.dto.RegionsBuildingsDto;
import com.populaire.projetguerrefroide.economy.production.ResourceGatheringOperationSystem;
import com.populaire.projetguerrefroide.map.*;
import com.populaire.projetguerrefroide.ui.view.SortType;
import com.populaire.projetguerrefroide.util.MutableInt;

import java.util.Comparator;
import java.util.List;

public class EconomyService {
    private final GameContext gameContext;
    private final WorldContext worldContext;
    private final ResourceGatheringOperationSystem rgoSystem;

    public EconomyService(GameContext gameContext, WorldContext worldContext) {
        this.gameContext = gameContext;
        this.worldContext = worldContext;
        this.rgoSystem = new ResourceGatheringOperationSystem();
    }

    public void initialize() {
        this.rgoSystem.initializeSize(this.gameContext.getEcsWorld());
    }

    public void hire() {
        this.rgoSystem.hire(this.gameContext.getEcsWorld());
    }

    public void produce() {
        this.rgoSystem.produce(this.gameContext.getEcsWorld());
    }

    public RegionsBuildingsDto prepareRegionsBuildingsDto(long countryId) {
        World ecsWorld = this.gameContext.getEcsWorld();

        LongOrderedSet regionIds = new LongOrderedSet();
        try (Query query = ecsWorld.query().with(Province.class).with(GeoHierarchy.class).build()) {
            query.iter(iter -> {
                for(int i = 0; i < iter.count(); i++) {
                    long ownerId = iter.fieldLong(Province.class, 0, "ownerId", i);
                    if(countryId == ownerId) {
                        long regionId = iter.fieldLong(GeoHierarchy.class, 1, "regionId", i);
                        regionIds.add(regionId);
                    }
                }
            });
        }

        List<RegionDto> regions = new ObjectList<>();
        LongSet.LongSetIterator iterator = regionIds.iterator();
        while(iterator.hasNext()) {
            Entity region = ecsWorld.obtainEntity(iterator.nextLong());
            RegionDto regionData = this.collectRegionData(countryId, region.id(), region.getName());
            regions.add(regionData);
        }

        return new RegionsBuildingsDto(regions);
    }

    public RegionsBuildingsDto prepareRegionsBuildingsDtoSorted(long countryId, SortType sortType) {
        World ecsWorld = this.gameContext.getEcsWorld();

        LongOrderedSet regionIds = new LongOrderedSet();
        try (Query query = ecsWorld.query().with(Province.class).with(GeoHierarchy.class).build()) {
            query.iter(iter -> {
                for(int i = 0; i < iter.count(); i++) {
                    long ownerId = iter.fieldLong(Province.class, 0, "ownerId", i);
                    if(countryId == ownerId) {
                        long regionId = iter.fieldLong(GeoHierarchy.class, 1, "regionId", i);
                        regionIds.add(regionId);
                    }
                }
            });
        }

        List<RegionDto> regions = new ObjectList<>();
        LongSet.LongSetIterator iterator = regionIds.iterator();
        while(iterator.hasNext()) {
            Entity region = ecsWorld.obtainEntity(iterator.nextLong());
            RegionDto regionData = this.collectRegionData(countryId, region.id(), region.getName());
            regions.add(regionData);
        }

        this.sortRegions(regions, sortType);

        return new RegionsBuildingsDto(regions);
    }

    private RegionDto collectRegionData(long countryId, long regionId, String regionNameId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        MutableInt populationAmount = new MutableInt(0);
        MutableInt buildingWorkerAmount = new MutableInt(0);
        List<BuildingDto> buildings = new ObjectList<>();

        try (Query provinceQuery = ecsWorld.query().with(Province.class).with(GeoHierarchy.class).build()) {
            provinceQuery.iter(iter -> {
                for (int i = 0; i < iter.count(); i++) {
                    long provinceId = iter.entity(i);
                    long ownerId = iter.fieldLong(Province.class, 0, "ownerId", i);
                    long parentRegionId = iter.fieldLong(GeoHierarchy.class, 0, "regionId", i);

                    if (countryId == ownerId && parentRegionId == regionId) {
                        int adults = this.getAmountAdults(provinceId);
                        populationAmount.increment(adults);
                    }
                }
            });
        }

        try (Query buildingQuery = ecsWorld.query().with(Building.class).build()) {
            buildingQuery.iter(iter -> {
                for (int i = 0; i < iter.count(); i++) {
                    long parentRegionId = iter.fieldLong(Building.class, 0, "parentId", i);

                    if (parentRegionId == regionId) {
                        long buildingId = iter.entity(i);
                        long buildingTypeId = iter.fieldLong(Building.class, 0, "typeId", i);
                        int buildingSize = iter.fieldInt(Building.class, 0, "size", i);

                        Entity buildingType = ecsWorld.obtainEntity(buildingTypeId);

                        if (buildingType.has(EconomyBuilding.class)) {
                            EconomyBuilding economyBuilding = buildingType.get(EconomyBuilding.class);
                            BuildingDto building = new BuildingDto(buildingId, buildingType.getName(), buildingSize, economyBuilding.maxLevel(), 0);
                            int workers = this.estimateWorkersForBuilding();
                            buildingWorkerAmount.increment(workers);
                            buildings.add(building);
                        }
                    }
                }
            });
        }

        byte developpementIndexValue = this.calculateDeveloppementIndex();
        int buildingWorkerRatio = 0;
        if (populationAmount.getValue() > 0) {
            buildingWorkerRatio = (int) ((buildingWorkerAmount.getValue() * 100.0f) / populationAmount.getValue());
        }

        return new RegionDto(regionNameId, populationAmount.getValue(), buildingWorkerAmount.getValue(), buildingWorkerRatio, developpementIndexValue, buildings);
    }

    private int estimateWorkersForBuilding() {
        return 0;
    }

    private byte calculateDeveloppementIndex() {
        return 0;
    }

    private void sortRegions(List<RegionDto> regions, SortType sortType) {
        switch (sortType) {
            case DEVELOPPEMENT_INDEX:
                regions.sort(Comparator.comparingInt(RegionDto::developpementIndexValue).reversed());
                break;
            case POPULATION:
                regions.sort(Comparator.comparingInt(RegionDto::populationAmount).reversed());
                break;
            case WORKFORCE:
                regions.sort(Comparator.comparingInt(RegionDto::buildingWorkerAmount).reversed());
                break;
        }
    }

    public float getResourceGatheringProduction(int provinceId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        long provinceEntityId = ecsWorld.lookup(String.valueOf(provinceId));
        if (provinceEntityId == -1) {
            return -1f;
        }
        return this.rgoSystem.getProduction(ecsWorld, provinceEntityId);
    }

    private int getAmountAdults(long provinceId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        Province province = ecsWorld.obtainEntity(provinceId).get(Province.class);
        return province.amountAdults();
    }
}

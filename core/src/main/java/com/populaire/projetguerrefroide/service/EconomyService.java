package com.populaire.projetguerrefroide.service;

import com.github.elebras1.flecs.*;
import com.github.tommyettinger.ds.*;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.dto.BuildingDto;
import com.populaire.projetguerrefroide.dto.RegionDto;
import com.populaire.projetguerrefroide.dto.RegionsBuildingsDto;
import com.populaire.projetguerrefroide.repository.QueryRepository;
import com.populaire.projetguerrefroide.system.economy.ResourceGatheringOperationHireSystem;
import com.populaire.projetguerrefroide.system.economy.ResourceGatheringOperationProduceSystem;
import com.populaire.projetguerrefroide.system.economy.ResourceGatheringOperationSizeSystem;
import com.populaire.projetguerrefroide.ui.view.SortType;
import com.populaire.projetguerrefroide.pojo.MutableInt;

import java.util.Comparator;
import java.util.List;

public class EconomyService {
    private final GameContext gameContext;
    private final QueryRepository queryRepository;
    private final ResourceGatheringOperationSizeSystem rgoSizeSystem;
    private final ResourceGatheringOperationHireSystem rgoHireSystem;
    private final ResourceGatheringOperationProduceSystem rgoProduceSystem;

    public EconomyService(GameContext gameContext, QueryRepository queryRepository) {
        this.gameContext = gameContext;
        this.queryRepository = queryRepository;
        this.rgoSizeSystem = new ResourceGatheringOperationSizeSystem(this.gameContext.getEcsWorld());
        this.rgoHireSystem = new ResourceGatheringOperationHireSystem(this.gameContext.getEcsWorld(), this);
        this.rgoProduceSystem = new ResourceGatheringOperationProduceSystem(this.gameContext.getEcsWorld(), this);
    }

    public RegionsBuildingsDto prepareRegionsBuildingsDto(long countryId) {
        World ecsWorld = this.gameContext.getEcsWorld();

        LongOrderedSet regionIds = new LongOrderedSet();
        Query query = this.queryRepository.getProvincesWithGeoHierarchy();
        query.iter(iter -> {
            Field<Province> provinceField = iter.field(Province.class, 0);
            Field<GeoHierarchy> geoHierarchyField = iter.field(GeoHierarchy.class, 1);
            for(int i = 0; i < iter.count(); i++) {
                ProvinceView provinceView = provinceField.getMutView(i);
                if(countryId == provinceView.ownerId()) {
                    GeoHierarchyView geoHierarchyView = geoHierarchyField.getMutView(i);
                    regionIds.add(geoHierarchyView.regionId());
                }
            }
        });

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
        Query query = this.queryRepository.getProvincesWithGeoHierarchy();
        query.iter(iter -> {
            Field<Province> provinceField = iter.field(Province.class, 0);
            Field<GeoHierarchy> geoHierarchyField = iter.field(GeoHierarchy.class, 1);
            for(int i = 0; i < iter.count(); i++) {
                ProvinceView provinceView = provinceField.getMutView(i);
                if(countryId == provinceView.ownerId()) {
                    GeoHierarchyView geoHierarchyView = geoHierarchyField.getMutView(i);
                    regionIds.add(geoHierarchyView.regionId());
                }
            }
        });

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

    public int getMaxWorkers(World ecsWorld, long resourceGoodId, int resourceGoodSize) {
        EntityView resourceGoodView = ecsWorld.obtainEntityView(resourceGoodId);
        ResourceProductionView resourceProductionView = resourceGoodView.getMutView(ResourceProduction.class);
        EntityView productionTypeEntityView = ecsWorld.obtainEntityView(resourceProductionView.productionTypeId());
        ProductionTypeView productionTypeDataView = productionTypeEntityView.getMutView(ProductionType.class);
        return resourceGoodSize * productionTypeDataView.workforce();
    }

    private RegionDto collectRegionData(long countryId, long regionId, String regionNameId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        MutableInt populationAmount = new MutableInt(0);
        MutableInt buildingWorkerAmount = new MutableInt(0);
        List<BuildingDto> buildings = new ObjectList<>();

        Query provinceQuery = this.queryRepository.getProvincesWithGeoHierarchy();
        provinceQuery.iter(iter -> {
            Field<Province> provinceField = iter.field(Province.class, 0);
            Field<GeoHierarchy> geoHierarchyField = iter.field(GeoHierarchy.class, 1);
            for (int i = 0; i < iter.count(); i++) {
                ProvinceView provinceView = provinceField.getMutView(i);
                GeoHierarchyView geoHierarchyView = geoHierarchyField.getMutView(i);
                if (countryId == provinceView.ownerId() && geoHierarchyView.regionId() == regionId) {
                    populationAmount.increment(provinceView.amountAdults());
                }
            }
        });

        Query buildingQuery = this.queryRepository.getBuildings();
        buildingQuery.iter(iter -> {
            Field<Building> buildingField = iter.field(Building.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                BuildingView buildingView = buildingField.getMutView(i);
                if (buildingView.parentId() == regionId) {
                    long buildingId = iter.entity(i);

                    EntityView buildingTypeView = ecsWorld.obtainEntityView(buildingView.typeId());

                    if (buildingTypeView.has(EconomyBuilding.class)) {
                        EconomyBuildingView economyBuildingView = buildingTypeView.getMutView(EconomyBuilding.class);
                        BuildingDto building = new BuildingDto(buildingId, buildingTypeView.getName(), buildingView.size(), economyBuildingView.maxLevel(), 0);
                        int workers = this.estimateWorkersForBuilding();
                        buildingWorkerAmount.increment(workers);
                        buildings.add(building);
                    }
                }
            }
        });

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

    public float getResourceGatheringProduction(String provinceNameId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        long provinceEntityId = ecsWorld.lookup(provinceNameId);
        if (provinceEntityId == -1) {
            return -1f;
        }
        return this.getProduction(ecsWorld, provinceEntityId);
    }

    private float getProduction(World ecsWorld, long provinceEntityId) {
        Entity provinceEntity = ecsWorld.obtainEntity(provinceEntityId);
        ResourceGathering state = provinceEntity.get(ResourceGathering.class);
        if (state != null) {
            return state.production();
        }
        return -1f;
    }
}

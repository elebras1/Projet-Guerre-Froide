package com.populaire.projetguerrefroide.service;

import com.github.elebras1.flecs.*;
import com.github.tommyettinger.ds.LongIntMap;
import com.github.tommyettinger.ds.LongList;
import com.github.tommyettinger.ds.LongOrderedSet;
import com.github.tommyettinger.ds.LongSet;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.dto.BuildingSummaryDto;
import com.populaire.projetguerrefroide.dto.RegionDto;
import com.populaire.projetguerrefroide.pojo.MutableInt;
import com.populaire.projetguerrefroide.pojo.Pair;
import com.populaire.projetguerrefroide.repository.QueryRepository;
import com.populaire.projetguerrefroide.util.BuildingUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionService {
    private final GameContext gameContext;
    private final QueryRepository queryRepository;
    private final BuildingService buildingService;

    public RegionService(GameContext gameContext, BuildingService buildingService, QueryRepository queryRepository) {
        this.gameContext = gameContext;
        this.buildingService = buildingService;
        this.queryRepository = queryRepository;
    }

    public RegionDto buildDetails(long countryId, long regionId, String regionNameId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        MutableInt populationAmount = new MutableInt(0);
        MutableInt buildingWorkerAmount = new MutableInt(0);
        List<BuildingSummaryDto> buildings = new ObjectList<>();

        Query provinceQuery = this.queryRepository.getProvincesWithGeoHierarchy();
        provinceQuery.iter(iter -> {
            Field<Province> provinceField = iter.field(Province.class, 0);
            Field<GeoHierarchy> geoHierarchyField = iter.field(GeoHierarchy.class, 1);
            for (int i = 0; i < iter.count(); i++) {
                ProvinceView provinceView = provinceField.getMutView(i);
                GeoHierarchyView geoHierarchyView = geoHierarchyField.getMutView(i);
                if (countryId == provinceView.ownerId() && geoHierarchyView.regionId() == regionId) {
                    populationAmount.increment(provinceView.adultsAmount());
                }
            }
        });

        Query buildingQuery = this.queryRepository.getBuildings();
        buildingQuery.iter(iter -> {
            Field<Building> buildingField = iter.field(Building.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                EntityView buildingView = ecsWorld.obtainEntityView(iter.entity(i));
                BuildingView buildingDataView = buildingField.getMutView(i);
                EntityView parent = ecsWorld.obtainEntityView(buildingDataView.parentId());
                if(!parent.has(RegionInstance.class)) {
                    continue;
                }
                RegionInstanceView regionInstance = parent.getMutView(RegionInstance.class);
                if (regionInstance.regionId() == regionId && regionInstance.ownerId() == countryId) {
                    long buildingId = iter.entity(i);

                    EntityView buildingTypeView = ecsWorld.obtainEntityView(buildingDataView.typeId());

                    int levelsQueued = 0;
                    long expansionBuildingId = ecsWorld.lookup("expand_" + buildingId);
                    if(expansionBuildingId != 0) {
                        EntityView expansionBuildingView = ecsWorld.obtainEntityView(expansionBuildingId);
                        ExpansionBuildingView expansionBuildingDataView = expansionBuildingView.getMutView(ExpansionBuilding.class);
                        levelsQueued = expansionBuildingDataView.levelsQueued();
                    }

                    if (buildingTypeView.has(EconomyBuildingType.class)) {
                        EconomyBuildingTypeView economyBuildingTypeView = buildingTypeView.getMutView(EconomyBuildingType.class);
                        boolean isSuspended = buildingView.has(this.gameContext.getEcsConstants().suspended());
                        BuildingSummaryDto building = this.buildingService.buildSummary(buildingId);
                        int workers = this.buildingService.estimateWorkersForBuilding();
                        buildingWorkerAmount.increment(workers);
                        buildings.add(building);
                    }
                }
            }
        });

        buildings.sort(Comparator.comparingLong(BuildingSummaryDto::buildingId));

        byte developpementIndexValue = this.calculateDeveloppementIndex();
        int buildingWorkerRatio = 0;
        if (populationAmount.getValue() > 0) {
            buildingWorkerRatio = (int) ((buildingWorkerAmount.getValue() * 100.0f) / populationAmount.getValue());
        }

        return new RegionDto(regionNameId, populationAmount.getValue(), buildingWorkerAmount.getValue(), buildingWorkerRatio, developpementIndexValue, buildings);
    }

    public List<RegionDto> buildRegionsDetails(long countryId, LongOrderedSet regionIds, LongIntMap populationByRegion, World ecsWorld) {
        Map<Long, List<BuildingSummaryDto>> buildingsByRegion = new HashMap<>();
        Map<Long, Integer> workersByRegion = new HashMap<>();

        Query buildingQuery = this.queryRepository.getBuildings();
        buildingQuery.iter(iter -> {
            Field<Building> buildingField = iter.field(Building.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                BuildingView buildingDataView = buildingField.getMutView(i);
                EntityView parent = ecsWorld.obtainEntityView(buildingDataView.parentId());
                if (!parent.has(RegionInstance.class)) {
                    continue;
                }
                RegionInstanceView regionInstance = parent.getMutView(RegionInstance.class);
                if (regionInstance.ownerId() != countryId) {
                    continue;
                }
                long regionId = regionInstance.regionId();
                if (!regionIds.contains(regionId)) {
                    continue;
                }
                EntityView buildingTypeView = ecsWorld.obtainEntityView(buildingDataView.typeId());
                if (!buildingTypeView.has(EconomyBuildingType.class)) {
                    continue;
                }
                long buildingId = iter.entity(i);
                int levelsQueued = 0;
                long expansionBuildingId = ecsWorld.lookup("expand_" + buildingId);
                if (expansionBuildingId != 0) {
                    EntityView expansionBuildingView = ecsWorld.obtainEntityView(expansionBuildingId);
                    ExpansionBuildingView expansionBuildingDataView = expansionBuildingView.getMutView(ExpansionBuilding.class);
                    levelsQueued = expansionBuildingDataView.levelsQueued();
                }
                EconomyBuildingTypeView economyBuildingTypeView = buildingTypeView.getMutView(EconomyBuildingType.class);
                boolean isSuspended = ecsWorld.obtainEntityView(buildingId).has(this.gameContext.getEcsConstants().suspended());
                BuildingSummaryDto building = this.buildingService.buildSummary(buildingId);
                int workers = this.buildingService.estimateWorkersForBuilding();

                buildingsByRegion.computeIfAbsent(regionId, _ -> new ObjectList<>()).add(building);
                workersByRegion.merge(regionId, workers, Integer::sum);
            }
        });

        List<RegionDto> regions = new ObjectList<>(regionIds.size());
        LongSet.LongSetIterator iterator = regionIds.iterator();
        while (iterator.hasNext()) {
            long regionId = iterator.nextLong();
            Entity region = ecsWorld.obtainEntity(regionId);

            int population = populationByRegion.getOrDefault(regionId, 0);
            int workerAmount = workersByRegion.getOrDefault(regionId, 0);
            int workerRatio = population > 0 ? (int) ((workerAmount * 100.0f) / population) : 0;

            List<BuildingSummaryDto> buildings = buildingsByRegion.getOrDefault(regionId, new ObjectList<>());
            buildings.sort(Comparator.comparingLong(BuildingSummaryDto::buildingId));

            byte developpementIndexValue = this.calculateDeveloppementIndex();
            regions.add(new RegionDto(region.getName(), population, workerAmount, workerRatio, developpementIndexValue, buildings));
        }

        return regions;
    }

    public List<String> getColorBuildingsOrderByLevel(long regionId, long ownerId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        List<Pair<Integer, String>> validBuildings = new ObjectList<>();

        Query query = this.queryRepository.getBuildings();
        query.iter(iter -> {
            Field<Building> buildingField = iter.field(Building.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                BuildingView buildingView = buildingField.getMutView(i);
                EntityView parent = ecsWorld.obtainEntityView(buildingView.parentId());
                if(!parent.has(RegionInstance.class)) {
                    continue;
                }
                RegionInstanceView regionInstance = parent.getMutView(RegionInstance.class);
                if (regionInstance.regionId() == regionId && regionInstance.ownerId() == ownerId) {
                    EntityView buildingTypeView = ecsWorld.obtainEntityView(buildingView.typeId());
                    if (buildingTypeView.has(EconomyBuildingType.class)) {
                        String color = BuildingUtils.getColor(buildingTypeView.getName());
                        if (color != null) {
                            validBuildings.add(new Pair<>(buildingView.size(), color));
                        }
                    }
                }
            }
        });

        validBuildings.sort((a, b) -> Integer.compare(b.first(), a.first()));

        List<String> colors = new ObjectList<>(validBuildings.size());
        for (Pair<Integer, String> building: validBuildings) {
            colors.add(building.second());
        }

        return colors;
    }

    public int getNumberIndustry(long regionId, long ownerId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        MutableInt industryCount = new MutableInt(0);

        Query query = this.queryRepository.getBuildings();
        query.iter(iter -> {
            Field<Building> buildingField = iter.field(Building.class, 0);
            for(int i = 0; i < iter.count(); i++) {
                BuildingView buildingView = buildingField.getMutView(i);
                EntityView parent = ecsWorld.obtainEntityView(buildingView.parentId());
                if(!parent.has(RegionInstance.class)) {
                    continue;
                }
                RegionInstanceView regionInstance = parent.getMutView(RegionInstance.class);
                if(regionInstance.regionId() == regionId && regionInstance.ownerId() == ownerId) {
                    EntityView buildingTypeView = ecsWorld.obtainEntityView(buildingView.typeId());
                    if(buildingTypeView.has(EconomyBuildingType.class)) {
                        industryCount.increment();
                    }
                }
            }
        });

        return industryCount.getValue();
    }

    public List<String> getSpecialBuildingNames(long regionId, long ownerId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        List<String> specialBuildingNames = new ObjectList<>();

        Query query = this.queryRepository.getBuildings();
        query.iter(iter -> {
            Field<Building> buildingField = iter.field(Building.class, 0);
            for(int i = 0; i < iter.count(); i++) {
                BuildingView buildingView = buildingField.getMutView(i);
                EntityView parent = ecsWorld.obtainEntityView(buildingView.parentId());
                if(!parent.has(RegionInstance.class)) {
                    continue;
                }
                RegionInstanceView regionInstance = parent.getMutView(RegionInstance.class);
                if(regionInstance.regionId() == regionId && regionInstance.ownerId() == ownerId) {
                    EntityView buildingTypeView = ecsWorld.obtainEntityView(buildingView.typeId());
                    if(buildingTypeView.has(SpecialBuildingType.class)) {
                        specialBuildingNames.add(buildingTypeView.getName());
                    }
                }
            }
        });

        return specialBuildingNames;
    }

    public int getWorkerAmount(long regionId) {
        MutableInt workers = new MutableInt(0);

        Query query = this.queryRepository.getProvincesWithGeoHierarchy();
        query.iter(iter -> {
            Field<Province> provinceField = iter.field(Province.class, 0);
            Field<GeoHierarchy> geoField = iter.field(GeoHierarchy.class, 1);
            for(int i = 0; i < iter.count(); i++) {
                ProvinceView provinceView = provinceField.getMutView(i);
                GeoHierarchyView geoView = geoField.getMutView(i);
                if (geoView.regionId() == regionId) {
                    workers.increment(provinceView.adultsAmount());
                }
            }
        });

        return workers.getValue();
    }

    public List<String> getProvinceNameIdsOrderByPopulation(long regionId) {
        World ecsWorld = this.gameContext.getEcsWorld();

        LongList provinceIds = new LongList();
        Query query = this.queryRepository.getProvincesWithGeoHierarchy();
        query.iter(iter -> {
            Field<GeoHierarchy> geoField = iter.field(GeoHierarchy.class, 1);
            for(int i = 0; i < iter.count(); i++) {
                long provinceId = iter.entity(i);
                GeoHierarchyView geoView = geoField.getMutView(i);
                if (geoView.regionId() == regionId) {
                    provinceIds.add(provinceId);
                }
            }
        });

        provinceIds.sort((a, b) -> {
            EntityView provinceAView = ecsWorld.obtainEntityView(a);
            ProvinceView provinceDataAView = provinceAView.getMutView(Province.class);
            EntityView provinceBView = ecsWorld.obtainEntityView(b);
            ProvinceView provinceDataBView = provinceBView.getMutView(Province.class);
            return Integer.compare(provinceDataBView.adultsAmount(), provinceDataAView.adultsAmount());
        });

        List<String> result = new ObjectList<>();
        for (int provinceIndex = 0; provinceIndex < provinceIds.size(); provinceIndex++) {
            long id = provinceIds.get(provinceIndex);
            Entity province = ecsWorld.obtainEntity(id);
            result.add(province.getName());
        }

        return result;
    }

    public byte calculateDeveloppementIndex() {
        return 0;
    }

}

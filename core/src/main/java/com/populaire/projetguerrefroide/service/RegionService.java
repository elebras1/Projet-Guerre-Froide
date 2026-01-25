package com.populaire.projetguerrefroide.service;

import com.github.elebras1.flecs.*;
import com.github.tommyettinger.ds.LongList;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.dto.BuildingDto;
import com.populaire.projetguerrefroide.dto.RegionDto;
import com.populaire.projetguerrefroide.pojo.MutableInt;
import com.populaire.projetguerrefroide.pojo.Pair;
import com.populaire.projetguerrefroide.repository.QueryRepository;
import com.populaire.projetguerrefroide.util.BuildingUtils;

import java.util.List;

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
                        int workers = this.buildingService.estimateWorkersForBuilding();
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

    public int getPopulationAmount(long regionId) {
        MutableInt population = new MutableInt(0);

        Query query = this.queryRepository.getProvincesWithGeoHierarchy();
        query.iter(iter -> {
            Field<Province> provinceField = iter.field(Province.class, 0);
            Field<GeoHierarchy> geoField = iter.field(GeoHierarchy.class, 1);
            for(int i = 0; i < iter.count(); i++) {
                ProvinceView provinceView = provinceField.getMutView(i);
                GeoHierarchyView geoView = geoField.getMutView(i);
                if (geoView.regionId() == regionId) {
                    population.increment(provinceView.amountChildren() + provinceView.amountAdults() + provinceView.amountSeniors());
                }
            }
        });

        return population.getValue();
    }

    public List<String> getColorBuildingsOrderByLevel(long regionId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        List<Pair<Integer, String>> validBuildings = new ObjectList<>();

        Query query = this.queryRepository.getBuildings();
        query.iter(iter -> {
            Field<Building> buildingField = iter.field(Building.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                BuildingView buildingView = buildingField.getMutView(i);
                if (buildingView.parentId() == regionId) {
                    EntityView buildingTypeView = ecsWorld.obtainEntityView(buildingView.typeId());
                    if (buildingTypeView.has(EconomyBuilding.class)) {
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

    public int getNumberIndustry(long regionId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        MutableInt industryCount = new MutableInt(0);

        Query query = this.queryRepository.getBuildings();
        query.iter(iter -> {
            Field<Building> buildingField = iter.field(Building.class, 0);
            for(int i = 0; i < iter.count(); i++) {
                BuildingView buildingView = buildingField.getMutView(i);
                if(buildingView.parentId() == regionId) {
                    EntityView buildingTypeView = ecsWorld.obtainEntityView(buildingView.typeId());
                    if(buildingTypeView.has(EconomyBuilding.class)) {
                        industryCount.increment();
                    }
                }
            }
        });

        return industryCount.getValue();
    }

    public List<String> getSpecialBuildingNames(long regionId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        List<String> specialBuildingNames = new ObjectList<>();

        Query query = this.queryRepository.getBuildings();
        query.iter(iter -> {
            Field<Building> buildingField = iter.field(Building.class, 0);
            for(int i = 0; i < iter.count(); i++) {
                BuildingView buildingView = buildingField.getMutView(i);
                if(buildingView.parentId() == regionId) {
                    EntityView buildingTypeView = ecsWorld.obtainEntityView(buildingView.typeId());
                    if(buildingTypeView.has(SpecialBuilding.class)) {
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
                    workers.increment(provinceView.amountAdults());
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

        this.gameContext.getEcsWorld().scope(() -> {
            provinceIds.sort((a, b) -> {
                EntityView provinceAView = ecsWorld.obtainEntityView(a);
                ProvinceView provinceDataAView = provinceAView.getMutView(Province.class);
                EntityView provinceBView = ecsWorld.obtainEntityView(b);
                ProvinceView provinceDataBView = provinceBView.getMutView(Province.class);
                return Integer.compare(provinceDataBView.amountAdults(), provinceDataAView.amountAdults());
            });
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

package com.populaire.projetguerrefroide.service;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.github.elebras1.flecs.*;
import com.github.tommyettinger.ds.*;
import com.populaire.projetguerrefroide.adapter.graphics.WgProjection;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.dao.WorldDao;
import com.populaire.projetguerrefroide.dao.impl.MapDaoImpl;
import com.populaire.projetguerrefroide.dao.impl.WorldDaoImpl;
import com.populaire.projetguerrefroide.dto.*;
import com.populaire.projetguerrefroide.pojo.MapMode;
import com.populaire.projetguerrefroide.pojo.WorldData;
import com.populaire.projetguerrefroide.repository.QueryRepository;
import com.populaire.projetguerrefroide.ui.view.SortType;
import com.populaire.projetguerrefroide.util.*;
import com.populaire.projetguerrefroide.util.EcsConstants;

import java.util.List;
import java.util.Map;

public class WorldService {
    private final GameContext gameContext;
    private final WorldDao worldDao;
    private final QueryRepository queryRepository;
    private final EconomyService economyService;
    private final CountryService countryService;
    private MapService mapService;

    public WorldService(GameContext gameContext) {
        this.gameContext = gameContext;
        this.worldDao = new WorldDaoImpl();
        this.queryRepository = new QueryRepository(this.gameContext.getEcsWorld(), this.gameContext.getEcsConstants());
        this.economyService = new EconomyService(this.gameContext, this.queryRepository);
        this.countryService = new CountryService(this.gameContext);
    }

    public void createWorld() {
        WorldData worldData = this.worldDao.createWorld(this.gameContext);
        this.mapService = new MapService(this.gameContext, this.queryRepository, new MapDaoImpl(), this.countryService, worldData.provinces(), worldData.borders());
        this.gameContext.getEcsWorld().shrink();
    }

    public void renderWorld(WgProjection projection, OrthographicCamera cam, float time) {
        this.mapService.render(projection, cam, time);
    }

    public boolean selectProvince(int x, int y) {
        return this.mapService.selectProvince(x, y);
    }

    public boolean isProvinceSelected() {
        return this.mapService.getSelectedProvinceId() != -1;
    }

    public boolean setCountryPlayer() {
        return this.mapService.setCountryPlayer();
    }

    public boolean hoverLandProvince(int x, int y) {
        return this.mapService.getLandProvinceId(x, y) != 0;
    }

    public int getProvinceId(int x, int y) {
        long provinceId = this.mapService.getLandProvinceId(x, y);
        return Integer.parseInt(this.gameContext.getEcsWorld().obtainEntity(provinceId).getName());
    }

    public MapMode getMapMode() {
        return this.mapService.getMapMode();
    }

    public String getCountryIdOfHoveredProvince(int x, int y) {
        World ecsWorld = this.gameContext.getEcsWorld();
        Entity province = ecsWorld.obtainEntity(this.mapService.getLandProvinceId(x, y));
        Province provinceData = province.get(Province.class);
        return ecsWorld.obtainEntity(provinceData.ownerId()).getName();
    }

    public Position getPositionOfCapitalOfSelectedCountry() {
        World ecsWorld = this.gameContext.getEcsWorld();
        Entity selectedProvince = ecsWorld.obtainEntity(this.mapService.getSelectedProvinceId());
        Province selectedProvinceData = selectedProvince.get(Province.class);
        Entity countryOwner = ecsWorld.obtainEntity(selectedProvinceData.ownerId());
        Country countryOwnerData = countryOwner.get(Country.class);
        Entity capitalProvince = ecsWorld.obtainEntity(countryOwnerData.capitalId());
        long capitalPositionId = ecsWorld.lookup("province_" + capitalProvince.getName() + "_pos_default");
        Entity capitalPosition = ecsWorld.obtainEntity(capitalPositionId);
        return capitalPosition.get(Position.class);
    }

    public String getCountryPlayerNameId() {
        World ecsWorld = this.gameContext.getEcsWorld();
        Entity countryPlayer = ecsWorld.obtainEntity(this.mapService.getPlayerCountryId());
        return countryPlayer.getName();
    }

    public int getNumberOfProvinces() {
        return this.mapService.getNumberOfProvinces();
    }

    public int getRankingOfSelectedCountry() {
        return 0;
    }

    public CountrySummaryDto prepareCountrySummaryDto(Map<String, String> localisation) {
        World ecsWorld = this.gameContext.getEcsWorld();
        Entity selectedProvince = ecsWorld.obtainEntity(this.mapService.getSelectedProvinceId());
        Province selectedProvinceData = selectedProvince.get(Province.class);
        Entity selectedCountry = ecsWorld.obtainEntity(selectedProvinceData.ownerId());
        Country countryData = selectedCountry.get(Country.class);
        Minister headOfState = this.getHeadOfState(selectedCountry.id());
        String portraitNameFile = "admin_type";
        if(headOfState.imageFileName() != null) {
            portraitNameFile = headOfState.imageFileName();
        }
        String population = ValueFormatter.formatValue(this.mapService.getPopulationAmountOfCountry(selectedCountry.id()), localisation);
        List<String> allies = this.getAlliesOfSelectedCountry(selectedCountry.id());
        String government = ecsWorld.obtainEntity(countryData.governmentId()).getName();

        return new CountrySummaryDto(selectedCountry.getName(), population, government, portraitNameFile, headOfState.name(), this.getColonizerId(selectedCountry.id()), allies);
    }

    public CountryDto prepareCountryDto(Map<String, String> localisation) {
        World ecsWorld = this.gameContext.getEcsWorld();
        Entity selectedProvince = ecsWorld.obtainEntity(this.mapService.getSelectedProvinceId());
        Province selectedProvinceData = selectedProvince.get(Province.class);
        String population = ValueFormatter.formatValue(this.mapService.getPopulationAmountOfCountry(selectedProvinceData.ownerId()), localisation);
        int manpower = 0;
        String grossDomesticProduct = ValueFormatter.formatValue(0, localisation);
        int money = 0;
        int supplies = 0;
        int fuel = 0;
        float diplomaticInfluence = 0;
        int uranium = 0;
        String dissent = ValueFormatter.formatValue(0, localisation);
        String nationalUnity = ValueFormatter.formatValue(0, localisation);

        return new CountryDto(population, manpower, grossDomesticProduct, money, supplies, fuel, diplomaticInfluence, uranium, dissent, nationalUnity);
    }

    public ProvinceDto prepareProvinceDto(Map<String, String> localisation) {
        World ecsWorld = this.gameContext.getEcsWorld();
        Entity selectedProvince = ecsWorld.obtainEntity(this.mapService.getSelectedProvinceId());
        Province selectedProvinceData = selectedProvince.get(Province.class);
        GeoHierarchy selectedProvinceGeo = selectedProvince.get(GeoHierarchy.class);
        Entity region = ecsWorld.obtainEntity(selectedProvinceGeo.regionId());
        String provinceNameId = selectedProvince.getName();
        String regionNameId = region.getName();
        Entity terrain = ecsWorld.obtainEntity(selectedProvinceData.terrainId());
        String terrainImage = terrain.getName();
        String resourceImage = this.mapService.getResourceGoodName(selectedProvince.id());
        String populationRegion = this.getPopulationRegionOfSelectedProvince(localisation);
        String workersRegion = this.getWorkersRegionOfSelectedProvince(localisation);
        String populationProvince = ValueFormatter.formatValue(this.mapService.getPopulationAmountOfProvince(selectedProvince.id()), localisation);
        int developmentIndexRegion = 0;
        int incomeRegion = 0;
        int numberIndustryRegion = this.getNumberIndustry(region.id());
        Entity country = ecsWorld.obtainEntity(selectedProvinceData.ownerId());
        String countryNameId = country.getName();
        String colonizerId = this.getColonizerId(country.id());
        List<String> flagCountriesCore = this.getCountriesCoreOfSelectedProvince();
        float resourceProduced = this.economyService.getResourceGatheringProduction(provinceNameId);
        List<String> provinceIdsRegion = this.getProvinceIdsOrderByPopulation(region.id());
        DevelopementBuildingLevelDto developmentBuildingLevel = this.getDevelopementBuildingLevel(selectedProvince.id());
        List<String> specialBuildings = this.getSpecialBuildingNames(region.id());
        List<String> colorsBuilding = this.getColorBuildingsOrderByLevel(region.id());

        return new ProvinceDto(provinceNameId, regionNameId, terrainImage, resourceImage, populationRegion, workersRegion, developmentIndexRegion, incomeRegion, numberIndustryRegion, countryNameId, colonizerId, flagCountriesCore, resourceProduced, 0, 0, populationProvince, 0f, 0f, provinceIdsRegion, developmentBuildingLevel, specialBuildings, colorsBuilding);
    }

    public void changeMapMode(String mapMode) {
        this.mapService.changeMapMode(mapMode);
    }

    public ObjectIntMap<String> getCulturesOfHoveredProvince(int x, int y) {
        long provinceId = this.mapService.getLandProvinceId(x, y);
        Entity province = this.gameContext.getEcsWorld().obtainEntity(provinceId);
        Province provinceData = province.get(Province.class);
        int amountAdults = provinceData.amountAdults();
        CultureDistribution cultureDistribution = province.get(CultureDistribution.class);
        long[] provinceCultureIds = cultureDistribution.populationIds();
        int[] provinceCultureValues = cultureDistribution.populationAmounts();
        return this.calculatePercentageDistributionFromProvinceData(provinceCultureIds, provinceCultureValues, amountAdults);
    }

    public ObjectIntMap<String> getReligionsOfHoveredProvince(int x, int y) {
        long provinceId = this.mapService.getLandProvinceId(x, y);
        Entity province = this.gameContext.getEcsWorld().obtainEntity(provinceId);
        Province provinceData = province.get(Province.class);
        int amountAdults = provinceData.amountAdults();
        ReligionDistribution religionDistribution = province.get(ReligionDistribution.class);
        long[] provinceReligionIds = religionDistribution.populationIds();
        int[] provinceReligionValues = religionDistribution.populationAmounts();

        return this.calculatePercentageDistributionFromProvinceData(provinceReligionIds, provinceReligionValues, amountAdults);
    }

    public String getColonizerIdOfSelectedProvince() {
        Entity selectedProvince = this.gameContext.getEcsWorld().obtainEntity(this.mapService.getSelectedProvinceId());
        Province selectedProvinceData = selectedProvince.get(Province.class);
        return this.getColonizerId(selectedProvinceData.ownerId());
    }

    public String getColonizerIdOfCountryPlayer() {
        return this.getColonizerId(this.mapService.getPlayerCountryId());
    }

    public String getColonizerIdOfHoveredProvince(int x, int y) {
        Entity province = this.gameContext.getEcsWorld().obtainEntity(this.mapService.getLandProvinceId(x, y));
        Province provinceData = province.get(Province.class);
        return this.getColonizerId(provinceData.ownerId());
    }

    public float getResourceGoodsProduction() {
        long selectedProvinceId = this.mapService.getSelectedProvinceId();
        if(selectedProvinceId == -1) {
            return -1;
        }
        Entity selectedProvince = this.gameContext.getEcsWorld().obtainEntity(selectedProvinceId);
        return this.economyService.getResourceGatheringProduction(selectedProvince.getName());
    }

    public RegionsBuildingsDto prepareRegionsBuildingsDto() {
        return this.economyService.prepareRegionsBuildingsDto(this.mapService.getPlayerCountryId());
    }

    public RegionsBuildingsDto prepareRegionsBuildingsDtoSorted(SortType sortType) {
        return this.economyService.prepareRegionsBuildingsDtoSorted(this.mapService.getPlayerCountryId(), sortType);
    }

    public String getColonizerId(long countryId) {
        return this.countryService.getColonizerId(countryId);
    }

    private ObjectIntMap<String> calculatePercentageDistributionFromProvinceData(long[] provinceElementIds, int[] provinceElementValues, int amountAdults) {
        ObjectIntOrderedMap<String> elementPercentages = new ObjectIntOrderedMap<>();
        World ecsWorld = this.gameContext.getEcsWorld();
        int total = 0;
        int biggestElementIndex = -1;
        for(int elementIndex = 0; elementIndex < provinceElementIds.length && provinceElementValues[elementIndex] != 0; elementIndex++) {
            int amount = provinceElementValues[elementIndex];
            long elementId = provinceElementIds[elementIndex];
            if(biggestElementIndex == -1 || amount > provinceElementValues[biggestElementIndex]) {
                biggestElementIndex = elementIndex;
            }
            if(amountAdults != 0) {
                int percentage = (int) ((amount / (float) amountAdults) * 100);
                total += percentage;
                elementPercentages.put(ecsWorld.obtainEntity(elementId).getName(), percentage);
            } else {
                elementPercentages.put(ecsWorld.obtainEntity(elementId).getName(), 0);
            }
        }

        if(total != 100 && biggestElementIndex != -1) {
            int difference = 100 - total;
            String biggestElementName = ecsWorld.obtainEntity(provinceElementIds[biggestElementIndex]).getName();
            elementPercentages.put(biggestElementName, elementPercentages.get(biggestElementName) + difference);
        }

        this.sortByValueDescending(elementPercentages);

        return elementPercentages;
    }

    private String getPopulationRegionOfSelectedProvince(Map<String, String> localisation) {
        World ecsWorld = this.gameContext.getEcsWorld();

        Entity selectedProvince = ecsWorld.obtainEntity(this.mapService.getSelectedProvinceId());
        GeoHierarchy selectedProvinceGeo = selectedProvince.get(GeoHierarchy.class);
        MutableInt population = new MutableInt(0);

        Query query = this.queryRepository.getProvincesWithGeoHierarchy();
        query.iter(iter -> {
            Field<Province> provinceField = iter.field(Province.class, 0);
            Field<GeoHierarchy> geoField = iter.field(GeoHierarchy.class, 1);
            for(int i = 0; i < iter.count(); i++) {
                ProvinceView provinceView = provinceField.getMutView(i);
                GeoHierarchyView geoView = geoField.getMutView(i);
                if (geoView.regionId() == selectedProvinceGeo.regionId()) {
                    population.increment(provinceView.amountChildren() + provinceView.amountAdults() + provinceView.amountSeniors());
                }
            }
        });

        return ValueFormatter.formatValue(population.getValue(), localisation);
    }

    private String getWorkersRegionOfSelectedProvince(Map<String, String> localisation) {
        World ecsWorld = this.gameContext.getEcsWorld();

        Entity selectedProvince = ecsWorld.obtainEntity(this.mapService.getSelectedProvinceId());
        GeoHierarchy selectedProvinceGeo = selectedProvince.get(GeoHierarchy.class);
        MutableInt workers = new MutableInt(0);

        Query query = this.queryRepository.getProvincesWithGeoHierarchy();
        query.iter(iter -> {
            Field<Province> provinceField = iter.field(Province.class, 0);
            Field<GeoHierarchy> geoField = iter.field(GeoHierarchy.class, 1);
            for(int i = 0; i < iter.count(); i++) {
                ProvinceView provinceView = provinceField.getMutView(i);
                GeoHierarchyView geoView = geoField.getMutView(i);
                if (geoView.regionId() == selectedProvinceGeo.regionId()) {
                    workers.increment(provinceView.amountAdults());
                }
            }
        });

        return ValueFormatter.formatValue(workers.getValue(), localisation);
    }

    private List<String> getCountriesCoreOfSelectedProvince() {
        World ecsWorld = this.gameContext.getEcsWorld();
        long selectedProvinceId = this.mapService.getSelectedProvinceId();
        Entity provinceEntity = ecsWorld.obtainEntity(selectedProvinceId);
        Province provinceData = provinceEntity.get(Province.class);
        List<String> countriesCore = new ObjectList<>();
        for (int i = 0; i < provinceData.coreIds().length; i++) {
            long coreCountryId = provinceData.coreIds()[i];
            if (coreCountryId == 0) {
                continue;
            }

            String countryNameId = ecsWorld.obtainEntity(coreCountryId).getName();
            countriesCore.add(countryNameId);
        }

        return countriesCore;
    }

    private DevelopementBuildingLevelDto getDevelopementBuildingLevel(long provinceId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        MutableInt navalBaseLevel = new MutableInt(0);
        MutableInt airBaseLevel = new MutableInt(0);
        MutableInt radarStationLevel = new MutableInt(0);
        MutableInt antiAircraftGunsLevel = new MutableInt(0);

        try(Query query = ecsWorld.query().with(Building.class).build()) {
            query.iter(iter -> {
                Field<Building> buildingField = iter.field(Building.class, 0);
                for(int i = 0; i < iter.count(); i++) {
                    BuildingView buildingView = buildingField.getMutView(i);
                    if(buildingView.parentId() != provinceId) {
                        continue;
                    }

                    Entity buildingType = ecsWorld.obtainEntity(buildingView.typeId());

                    switch (buildingType.getName()) {
                        case "naval_base" -> navalBaseLevel.setValue(buildingView.size());
                        case "air_base" -> airBaseLevel.setValue(buildingView.size());
                        case "radar_station" -> radarStationLevel.setValue(buildingView.size());
                        case "anti_air" -> antiAircraftGunsLevel.setValue(buildingView.size());
                    }
                }
            });
        }

        return new DevelopementBuildingLevelDto((byte) navalBaseLevel.getValue(), (byte)  airBaseLevel.getValue(), (byte)  radarStationLevel.getValue(), (byte)  antiAircraftGunsLevel.getValue());
    }

    private List<String> getProvinceIdsOrderByPopulation(long regionId) {
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
            result.add(String.valueOf(id));
        }

        return result;
    }

    private List<String> getColorBuildingsOrderByLevel(long regionId) {
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

    private int getNumberIndustry(long regionId) {
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

    private List<String> getSpecialBuildingNames(long regionId) {
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

    private void sortByValueDescending(ObjectIntOrderedMap<String> map) {
        ObjectList<String> keys = map.order();
        int size = map.size();
        for(int i = 1; i < size; i++){
            String key = keys.get(i);
            int value = map.get(key);
            int j = i - 1;
            while(j >= 0 && map.get(keys.get(j)) < value){
                keys.set(j + 1, keys.get(j));
                j--;
            }
            keys.set(j + 1, key);
        }
    }

    private List<String> getAlliesOfSelectedCountry(long countryId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        EcsConstants ecsConstants = this.gameContext.getEcsConstants();

        List<String> allies = new ObjectList<>();

        Entity country = ecsWorld.obtainEntity(countryId);
        if (country == null) {
            return allies;
        }

        long[] relations = new long[] {
                ecsConstants.alliedWith(),
                ecsConstants.guarantees(),
                ecsConstants.isGuaranteedBy(),
                ecsConstants.isPuppetMasterOf(),
                ecsConstants.isPuppetOf(),
                ecsConstants.colonizes(),
                ecsConstants.isColonyOf()
        };

        for (long relation : relations) {
            int i = 0;
            long alliedCountryId = country.target(relation, i);

            while (alliedCountryId != 0) {

                if (relation != ecsConstants.isColonyOf()) {
                    Entity alliedCountry = ecsWorld.obtainEntity(alliedCountryId);
                    if (alliedCountry != null) {
                        String alliedCountryNameId = alliedCountry.getName();
                        if (!allies.contains(alliedCountryNameId)) {
                            allies.add(alliedCountryNameId);
                        }
                    }
                }

                i++;
                alliedCountryId = country.target(relation, i);
            }
        }

        return allies;
    }

    private Minister getHeadOfState(long countryId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        EcsConstants ecsConstants = this.gameContext.getEcsConstants();

        Entity country = ecsWorld.obtainEntity(countryId);
        Country countryData = country.get(Country.class);
        long headOfStateId = countryData.headOfStateId();

        long countryColonizerId = country.target(ecsConstants.isColonyOf());
        if(countryColonizerId != 0) {
            Entity colonizerCountry = ecsWorld.obtainEntity(countryColonizerId);
            Country colonizerCountryData = colonizerCountry.get(Country.class);
            headOfStateId = colonizerCountryData.headOfStateId();
        }

        Minister headOfState = null;
        if(headOfStateId != 0) {
            Entity entityMinister = ecsWorld.obtainEntity(headOfStateId);
            headOfState = entityMinister.get(Minister.class);
        }

        return headOfState;
    }

    public void dispose() {
        this.mapService.dispose();
        this.queryRepository.dispose();
    }
}

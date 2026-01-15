package com.populaire.projetguerrefroide.service;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.Query;
import com.github.elebras1.flecs.World;
import com.github.tommyettinger.ds.*;
import com.populaire.projetguerrefroide.adapter.graphics.WgProjection;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.dao.WorldDao;
import com.populaire.projetguerrefroide.dao.impl.WorldDaoImpl;
import com.populaire.projetguerrefroide.dto.*;
import com.populaire.projetguerrefroide.map.*;
import com.populaire.projetguerrefroide.ui.view.SortType;
import com.populaire.projetguerrefroide.util.*;

import java.util.List;
import java.util.Map;

public class WorldService {
    private final GameContext gameContext;
    private final AsyncExecutor asyncExecutor;
    private final WorldDao worldDao;
    private WorldManager worldManager;
    private EconomyService economyService;

    public WorldService(GameContext gameContext) {
        this.gameContext = gameContext;
        this.asyncExecutor = new AsyncExecutor(2);
        this.worldDao = new WorldDaoImpl();
    }

    public void createWorld() {
        this.worldManager = this.worldDao.createWorld(this.gameContext);
        this.economyService = new EconomyService(this.gameContext, this.worldManager);
        this.gameContext.getEcsWorld().shrink();
    }

    public AsyncExecutor getAsyncExecutor() {
        return this.asyncExecutor;
    }

    public void renderWorld(WgProjection projection, OrthographicCamera cam, float time) {
        this.worldManager.render(projection, cam, time);
    }

    public boolean selectProvince(int x, int y) {
        return this.worldManager.selectProvince(x, y);
    }

    public boolean isProvinceSelected() {
        return this.worldManager.getSelectedProvinceId() != -1;
    }

    public boolean setCountryPlayer() {
        return this.worldManager.setCountryPlayer();
    }

    public boolean hoverLandProvince(int x, int y) {
        return this.worldManager.getLandProvinceId(x, y) != 0;
    }

    public int getProvinceId(int x, int y) {
        long provinceId = this.worldManager.getLandProvinceId(x, y);
        return Integer.parseInt(this.gameContext.getEcsWorld().obtainEntity(provinceId).getName());
    }

    public MapMode getMapMode() {
        return this.worldManager.getMapMode();
    }

    public String getCountryIdOfHoveredProvince(int x, int y) {
        World ecsWorld = this.gameContext.getEcsWorld();
        Entity province = ecsWorld.obtainEntity(this.worldManager.getLandProvinceId(x, y));
        Province provinceData = province.get(Province.class);
        return ecsWorld.obtainEntity(provinceData.ownerId()).getName();
    }

    public Position getPositionOfCapitalOfSelectedCountry() {
        World ecsWorld = this.gameContext.getEcsWorld();
        Entity selectedProvince = ecsWorld.obtainEntity(this.worldManager.getSelectedProvinceId());
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
        Entity countryPlayer = ecsWorld.obtainEntity(this.worldManager.getPlayerCountryId());
        return countryPlayer.getName();
    }

    public int getNumberOfProvinces() {
        return this.worldManager.getNumberOfProvinces();
    }

    public int getRankingOfSelectedCountry() {
        return 0;
    }

    public CountrySummaryDto prepareCountrySummaryDto(Map<String, String> localisation) {
        World ecsWorld = this.gameContext.getEcsWorld();
        Entity selectedProvince = ecsWorld.obtainEntity(this.worldManager.getSelectedProvinceId());
        Province selectedProvinceData = selectedProvince.get(Province.class);
        Entity selectedCountry = ecsWorld.obtainEntity(selectedProvinceData.ownerId());
        Country countryData = selectedCountry.get(Country.class);
        Minister headOfState = this.getHeadOfState(selectedCountry.id());
        String portraitNameFile = "admin_type";
        if(headOfState.imageFileName() != null) {
            portraitNameFile = headOfState.imageFileName();
        }
        String population = ValueFormatter.formatValue(this.worldManager.getPopulationAmountOfCountry(selectedCountry.id()), localisation);
        List<String> allies = this.getAlliesOfSelectedCountry(selectedCountry.id());
        String government = ecsWorld.obtainEntity(countryData.governmentId()).getName();

        return new CountrySummaryDto(selectedCountry.getName(), population, government, portraitNameFile, headOfState.name(), this.worldManager.getColonizerId(selectedCountry.id()), allies);
    }

    public CountryDto prepareCountryDto(Map<String, String> localisation) {
        World ecsWorld = this.gameContext.getEcsWorld();
        Entity selectedProvince = ecsWorld.obtainEntity(this.worldManager.getSelectedProvinceId());
        Province selectedProvinceData = selectedProvince.get(Province.class);
        String population = ValueFormatter.formatValue(this.worldManager.getPopulationAmountOfCountry(selectedProvinceData.ownerId()), localisation);
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
        Entity selectedProvince = ecsWorld.obtainEntity(this.worldManager.getSelectedProvinceId());
        Province selectedProvinceData = selectedProvince.get(Province.class);
        GeoHierarchy selectedProvinceGeo = selectedProvince.get(GeoHierarchy.class);
        Entity region = ecsWorld.obtainEntity(selectedProvinceGeo.regionId());
        String provinceNameId = selectedProvince.getName();
        String regionNameId = region.getName();
        Entity terrain = ecsWorld.obtainEntity(selectedProvinceData.terrainId());
        String terrainImage = terrain.getName();
        String resourceImage = this.worldManager.getResourceGoodName(selectedProvince.id());
        String populationRegion = this.getPopulationRegionOfSelectedProvince(localisation);
        String workersRegion = this.getWorkersRegionOfSelectedProvince(localisation);
        String populationProvince = ValueFormatter.formatValue(this.worldManager.getPopulationAmountOfProvince(selectedProvince.id()), localisation);
        int developmentIndexRegion = 0;
        int incomeRegion = 0;
        int numberIndustryRegion = this.getNumberIndustry(region.id());
        Entity country = ecsWorld.obtainEntity(selectedProvinceData.ownerId());
        String countryNameId = country.getName();
        String colonizerId = this.worldManager.getColonizerId(country.id());
        List<String> flagCountriesCore = this.getCountriesCoreOfSelectedProvince();
        float resourceProduced = this.economyService.getResourceGatheringProduction(provinceNameId);
        List<String> provinceIdsRegion = this.getProvinceIdsOrderByPopulation(region.id());
        DevelopementBuildingLevelDto developmentBuildingLevel = this.getDevelopementBuildingLevel(selectedProvince.id());
        List<String> specialBuildings = this.getSpecialBuildingNames(region.id());
        List<String> colorsBuilding = this.getColorBuildingsOrderByLevel(region.id());

        return new ProvinceDto(provinceNameId, regionNameId, terrainImage, resourceImage, populationRegion, workersRegion, developmentIndexRegion, incomeRegion, numberIndustryRegion, countryNameId, colonizerId, flagCountriesCore, resourceProduced, 0, 0, populationProvince, 0f, 0f, provinceIdsRegion, developmentBuildingLevel, specialBuildings, colorsBuilding);
    }

    public void changeMapMode(String mapMode) {
        this.worldManager.changeMapMode(mapMode);
    }

    public ObjectIntMap<String> getCulturesOfHoveredProvince(int x, int y) {
        long provinceId = this.worldManager.getLandProvinceId(x, y);
        Entity province = this.gameContext.getEcsWorld().obtainEntity(provinceId);
        Province provinceData = province.get(Province.class);
        int amountAdults = provinceData.amountAdults();
        CultureDistribution cultureDistribution = province.get(CultureDistribution.class);
        long[] provinceCultureIds = cultureDistribution.populationIds();
        int[] provinceCultureValues = cultureDistribution.populationAmounts();
        return this.calculatePercentageDistributionFromProvinceData(provinceCultureIds, provinceCultureValues, amountAdults);
    }

    public ObjectIntMap<String> getReligionsOfHoveredProvince(int x, int y) {
        long provinceId = this.worldManager.getLandProvinceId(x, y);
        Entity province = this.gameContext.getEcsWorld().obtainEntity(provinceId);
        Province provinceData = province.get(Province.class);
        int amountAdults = provinceData.amountAdults();
        ReligionDistribution religionDistribution = province.get(ReligionDistribution.class);
        long[] provinceReligionIds = religionDistribution.populationIds();
        int[] provinceReligionValues = religionDistribution.populationAmounts();

        return this.calculatePercentageDistributionFromProvinceData(provinceReligionIds, provinceReligionValues, amountAdults);
    }

    public String getColonizerIdOfSelectedProvince() {
        Entity selectedProvince = this.gameContext.getEcsWorld().obtainEntity(this.worldManager.getSelectedProvinceId());
        Province selectedProvinceData = selectedProvince.get(Province.class);
        return this.worldManager.getColonizerId(selectedProvinceData.ownerId());
    }

    public String getColonizerIdOfCountryPlayer() {
        return this.worldManager.getColonizerId(this.worldManager.getPlayerCountryId());
    }

    public String getColonizerIdOfHoveredProvince(int x, int y) {
        Entity province = this.gameContext.getEcsWorld().obtainEntity(this.worldManager.getLandProvinceId(x, y));
        Province provinceData = province.get(Province.class);
        return this.worldManager.getColonizerId(provinceData.ownerId());
    }

    public float getResourceGoodsProduction() {
        long selectedProvinceId = this.worldManager.getSelectedProvinceId();
        if(selectedProvinceId == -1) {
            return -1;
        }
        Entity selectedProvince = this.gameContext.getEcsWorld().obtainEntity(selectedProvinceId);
        return this.economyService.getResourceGatheringProduction(selectedProvince.getName());
    }

    public RegionsBuildingsDto prepareRegionsBuildingsDto() {
        return this.economyService.prepareRegionsBuildingsDto(this.worldManager.getPlayerCountryId());
    }

    public RegionsBuildingsDto prepareRegionsBuildingsDtoSorted(SortType sortType) {
        return this.economyService.prepareRegionsBuildingsDtoSorted(this.worldManager.getPlayerCountryId(), sortType);
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

        Entity selectedProvince = ecsWorld.obtainEntity(this.worldManager.getSelectedProvinceId());
        GeoHierarchy selectedProvinceGeo = selectedProvince.get(GeoHierarchy.class);
        MutableInt population = new MutableInt(0);

        try (Query query = ecsWorld.query().with(Province.class).with(GeoHierarchy.class).build()) {
            query.iter(iter -> {
                for(int i = 0; i < iter.count(); i++) {
                    long provinceId = iter.entity(i);
                    long provinceRegionId = iter.fieldLong(GeoHierarchy.class, 1, "regionId", i);
                    if (provinceRegionId == selectedProvinceGeo.regionId()) {
                        population.increment(this.worldManager.getPopulationAmountOfProvince(provinceId));
                    }
                }
            });
        }

        return ValueFormatter.formatValue(population.getValue(), localisation);
    }

    private String getWorkersRegionOfSelectedProvince(Map<String, String> localisation) {
        World ecsWorld = this.gameContext.getEcsWorld();

        Entity selectedProvince = ecsWorld.obtainEntity(this.worldManager.getSelectedProvinceId());
        GeoHierarchy selectedProvinceGeo = selectedProvince.get(GeoHierarchy.class);
        MutableInt workers = new MutableInt(0);

        try (Query query = ecsWorld.query().with(Province.class).with(GeoHierarchy.class).build()) {
            query.iter(iter -> {
                for(int i = 0; i < iter.count(); i++) {
                    long provinceId = iter.entity(i);
                    long provinceRegionId = iter.fieldLong(GeoHierarchy.class, 1, "regionId", i);
                    if (provinceRegionId == selectedProvinceGeo.regionId()) {
                        workers.increment(this.worldManager.getAmountAdults(provinceId));
                    }
                }
            });
        }

        return ValueFormatter.formatValue(workers.getValue(), localisation);
    }

    private List<String> getCountriesCoreOfSelectedProvince() {
        World ecsWorld = this.gameContext.getEcsWorld();
        long selectedProvinceId = this.worldManager.getSelectedProvinceId();
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
                for(int i = 0; i < iter.count(); i++) {
                    long provinceBuildingId = iter.fieldLong(Building.class, 0, "parentId", i);
                    if(provinceBuildingId != provinceId) {
                        continue;
                    }

                    long buildingTypeId = iter.fieldLong(Building.class, 0, "typeId", i);
                    int buildingLevel = iter.fieldInt(Building.class, 0, "size", i);
                    Entity buildingType = ecsWorld.obtainEntity(buildingTypeId);

                    switch (buildingType.getName()) {
                        case "naval_base" -> navalBaseLevel.setValue(buildingLevel);
                        case "air_base" -> airBaseLevel.setValue(buildingLevel);
                        case "radar_station" -> radarStationLevel.setValue(buildingLevel);
                        case "anti_air" -> antiAircraftGunsLevel.setValue(buildingLevel);
                    }
                }
            });
        }

        return new DevelopementBuildingLevelDto((byte) navalBaseLevel.getValue(), (byte)  airBaseLevel.getValue(), (byte)  radarStationLevel.getValue(), (byte)  antiAircraftGunsLevel.getValue());
    }

    private List<String> getProvinceIdsOrderByPopulation(long regionId) {
        World ecsWorld = this.gameContext.getEcsWorld();

        LongList provinceIds = new LongList();
        try (Query query = ecsWorld.query().with(Province.class).with(GeoHierarchy.class).build()) {
            query.iter(iter -> {
                for(int i = 0; i < iter.count(); i++) {
                    long provinceId = iter.entity(i);
                    long provinceRegionId = iter.fieldLong(GeoHierarchy.class, 1, "regionId", i);
                    if (provinceRegionId == regionId) {
                        provinceIds.add(provinceId);
                    }
                }
            });
        }

        provinceIds.sort((a, b) -> {
            int populationA = ecsWorld.obtainEntity(a).get(Province.class).amountAdults();
            int populationB = ecsWorld.obtainEntity(b).get(Province.class).amountAdults();
            return Integer.compare(populationB, populationA);
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

        try (Query query = ecsWorld.query().with(Building.class).build()) {
            query.iter(iter -> {
                for (int i = 0; i < iter.count(); i++) {
                    long parentId = iter.fieldLong(Building.class, 0, "parentId", i);
                    if (parentId == regionId) {
                        long buildingTypeId = iter.fieldLong(Building.class, 0, "typeId", i);
                        Entity buildingType = ecsWorld.obtainEntity(buildingTypeId);
                        if (buildingType.has(EconomyBuilding.class)) {
                            String color = BuildingUtils.getColor(buildingType.getName());
                            if (color != null) {
                                int level = iter.fieldInt(Building.class, 0, "size", i);

                                validBuildings.add(new Pair<>(level, color));
                            }
                        }
                    }
                }
            });
        }

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

        try(Query query = ecsWorld.query().with(Building.class).build()) {
            query.iter(iter -> {
                for(int i = 0; i < iter.count(); i++) {
                    long regionBuildingId = iter.fieldLong(Building.class, 0, "parentId", i);
                    if(regionBuildingId == regionId) {
                        long buildingTypeId = iter.fieldLong(Building.class, 0, "typeId", i);
                        Entity buildingType = ecsWorld.obtainEntity(buildingTypeId);
                        if(buildingType.has(EconomyBuilding.class)) {
                            industryCount.increment();
                        }
                    }
                }
            });

        }

        return industryCount.getValue();
    }

    private List<String> getSpecialBuildingNames(long regionId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        List<String> specialBuildingNames = new ObjectList<>();
        try(Query query = ecsWorld.query().with(Building.class).build()) {
            query.iter(iter -> {
                for(int i = 0; i < iter.count(); i++) {
                    long regionBuildingId = iter.fieldLong(Building.class, 0, "parentId", i);
                    if(regionBuildingId == regionId) {
                        long buildingTypeId = iter.fieldLong(Building.class, 0, "typeId", i);
                        Entity buildingType = ecsWorld.obtainEntity(buildingTypeId);
                        if(buildingType.has(SpecialBuilding.class)) {
                            specialBuildingNames.add(buildingType.getName());
                        }
                    }
                }
            });

        }

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
        this.worldManager.dispose();
        this.asyncExecutor.dispose();
    }
}

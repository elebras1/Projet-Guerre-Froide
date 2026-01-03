package com.populaire.projetguerrefroide.service;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.Query;
import com.github.elebras1.flecs.World;
import com.github.tommyettinger.ds.*;
import com.populaire.projetguerrefroide.adapter.graphics.WgProjection;
import com.populaire.projetguerrefroide.component.Minister;
import com.populaire.projetguerrefroide.component.Position;
import com.populaire.projetguerrefroide.dao.WorldDao;
import com.populaire.projetguerrefroide.dao.impl.WorldDaoImpl;
import com.populaire.projetguerrefroide.dto.*;
import com.populaire.projetguerrefroide.economy.building.BuildingStore;
import com.populaire.projetguerrefroide.economy.building.BuildingType;
import com.populaire.projetguerrefroide.map.*;
import com.populaire.projetguerrefroide.screen.DateListener;
import com.populaire.projetguerrefroide.ui.view.SortType;
import com.populaire.projetguerrefroide.util.BuildingUtils;
import com.populaire.projetguerrefroide.util.EcsConstants;
import com.populaire.projetguerrefroide.util.MutableInt;
import com.populaire.projetguerrefroide.util.ValueFormatter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class WorldService implements DateListener {
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
        this.economyService = new EconomyService(this.worldManager);
    }

    public AsyncExecutor getAsyncExecutor() {
        return this.asyncExecutor;
    }

    public void renderWorld(WgProjection projection, OrthographicCamera cam, float time) {
        this.worldManager.render(projection, cam, time);
    }

    public void initializeEconomy() {
        this.economyService.initialize();
    }

    public boolean selectProvince(short x, short y) {
        return this.worldManager.selectProvince(x, y);
    }

    public boolean isProvinceSelected() {
        return this.worldManager.getSelectedProvinceId() != -1;
    }

    public boolean setCountryPlayer() {
        return this.worldManager.setCountryPlayer();
    }

    public boolean hoverLandProvince(short x, short y) {
        return this.worldManager.getLandProvinceId(x, y) != 0;
    }

    public short getProvinceId(short x, short y) {
        long provinceId = this.worldManager.getLandProvinceId(x, y);
        return Short.parseShort(this.gameContext.getEcsWorld().obtainEntity(provinceId).getName());
    }

    public MapMode getMapMode() {
        return this.worldManager.getMapMode();
    }

    public String getCountryIdOfHoveredProvince(short x, short y) {
        World ecsWorld = this.gameContext.getEcsWorld();
        EcsConstants ecsConstants = this.gameContext.getEcsConstants();
        Entity province = ecsWorld.obtainEntity(this.worldManager.getLandProvinceId(x, y));
        long countryOwnerId = province.target(ecsConstants.ownedBy());
        return ecsWorld.obtainEntity(countryOwnerId).getName();
    }

    public Position getPositionOfCapitalOfSelectedCountry() {
        World ecsWorld = this.gameContext.getEcsWorld();
        EcsConstants ecsConstants = this.gameContext.getEcsConstants();
        Entity selectedProvince = ecsWorld.obtainEntity(this.worldManager.getSelectedProvinceId());
        Entity countryOwner = ecsWorld.obtainEntity(selectedProvince.target(ecsConstants.ownedBy()));
        Entity capitalProvince = ecsWorld.obtainEntity(countryOwner.target(ecsConstants.hasCapital()));
        long capitalPositionId = ecsWorld.lookup("province_" + capitalProvince.getName() + "_pos_default");
        Entity capitalPosition = ecsWorld.obtainEntity(capitalPositionId);
        return capitalPosition.get(Position.class);
    }

    public String getCountryIdPlayer() {
        World ecsWorld = this.gameContext.getEcsWorld();
        Entity countryPlayer = ecsWorld.obtainEntity(this.worldManager.getPlayerCountryId());
        return countryPlayer.getName();
    }

    public short getNumberOfProvinces() {
        return this.worldManager.getNumberOfProvinces();
    }

    public int getRankingOfSelectedCountry() {
        return 0;
    }

    public CountrySummaryDto prepareCountrySummaryDto(Map<String, String> localisation) {
        World ecsWorld = this.gameContext.getEcsWorld();
        EcsConstants ecsConstants = this.gameContext.getEcsConstants();
        long selectedCountryId = ecsWorld.obtainEntity(this.worldManager.getSelectedProvinceId()).target(ecsConstants.ownedBy());
        Entity selectedCountry = ecsWorld.obtainEntity(selectedCountryId);
        Minister headOfState = this.getHeadOfState(selectedCountryId);
        String portraitNameFile = "admin_type";
        if(headOfState.imageFileName() != null) {
            portraitNameFile = headOfState.imageFileName();
        }
        String population = ValueFormatter.formatValue(this.worldManager.getPopulationAmountOfCountry(selectedCountryId), localisation);
        List<String> allies = this.getAlliesOfSelectedCountry(selectedCountryId);
        String government = ecsWorld.obtainEntity(selectedCountry.target(ecsConstants.hasGovernment())).getName();

        return new CountrySummaryDto(selectedCountry.getName(), population, government, portraitNameFile, headOfState.name(), this.worldManager.getColonizerId(selectedCountryId), allies);
    }

    public CountryDto prepareCountryDto(Map<String, String> localisation) {
        World ecsWorld = this.gameContext.getEcsWorld();
        EcsConstants ecsConstants = this.gameContext.getEcsConstants();
        long selectedCountryId = ecsWorld.obtainEntity(this.worldManager.getSelectedProvinceId()).target(ecsConstants.ownedBy());
        String population = ValueFormatter.formatValue(this.worldManager.getPopulationAmountOfCountry(selectedCountryId), localisation);
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
        EcsConstants ecsConstants = this.gameContext.getEcsConstants();
        Entity selectedProvince = ecsWorld.obtainEntity(this.worldManager.getSelectedProvinceId());
        Entity region = ecsWorld.obtainEntity(selectedProvince.target(ecsConstants.locatedInRegion()));
        String provinceNameId = selectedProvince.getName();
        String regionNameId = region.getName();
        Entity terrain = ecsWorld.obtainEntity(selectedProvince.target(ecsConstants.hasTerrain()));
        String terrainImage = terrain.getName();
        String resourceImage = this.worldManager.getResourceGoodName(selectedProvince.id());
        String populationRegion = this.getPopulationRegionOfSelectedProvince(localisation);
        String workersRegion = this.getWorkersRegionOfSelectedProvince(localisation);
        String populationProvince = ValueFormatter.formatValue(this.worldManager.getPopulationAmountOfProvince(selectedProvince.id()), localisation);
        int developmentIndexRegion = 0;
        int incomeRegion = 0;
        int numberIndustryRegion = this.getNumberIndustry(region.id());
        Entity country = ecsWorld.obtainEntity(selectedProvince.target(ecsConstants.ownedBy()));
        String countryNameId = country.getName();
        String colonizerId = this.worldManager.getColonizerId(country.id());
        List<String> flagCountriesCore = this.getCountriesCoreOfSelectedProvince();
        float resourceProduced = this.economyService.getResourceGoodsProduction(Integer.parseInt(provinceNameId));
        List<String> provinceIdsRegion = this.getProvinceIdsOrderByPopulation(region.id());
        DevelopementBuildingLevelDto developmentBuildingLevel = this.getDevelopementBuildingLevel(Integer.parseInt(provinceNameId));
        List<String> specialBuildings = this.getSpecialBuildingNames(region.id());
        List<String> colorsBuilding = this.getColorBuildingsOrderByLevel(region.id());

        return new ProvinceDto(provinceNameId, regionNameId, terrainImage, resourceImage, populationRegion, workersRegion, developmentIndexRegion, incomeRegion, numberIndustryRegion, countryNameId, colonizerId, flagCountriesCore, resourceProduced, 0, 0, populationProvince, 0f, 0f, provinceIdsRegion, developmentBuildingLevel, specialBuildings, colorsBuilding);
    }

    public void changeMapMode(String mapMode) {
        this.worldManager.changeMapMode(mapMode);
    }

    public ObjectIntMap<String> getCulturesOfHoveredProvince(short x, short y) {
        long provinceId = this.worldManager.getLandProvinceId(x, y);
        Entity province = this.gameContext.getEcsWorld().obtainEntity(provinceId);
        int provinceNameId = Integer.parseInt(province.getName());
        ProvinceStore provinceStore = this.worldManager.getProvinceStore();
        int provinceIndex = provinceStore.getIndexById().get(provinceNameId);
        int amountAdults = provinceStore.getAmountAdults().get(provinceIndex);
        LongList provinceCultureIds = provinceStore.getCultureIds();
        IntList provinceCultureValues = provinceStore.getCultureValues();
        int startIndex = provinceStore.getCultureStarts().get(provinceIndex);
        int endIndex = startIndex + provinceStore.getCultureCounts().get(provinceIndex);
        return this.calculatePercentageDistributionFromProvinceData(provinceCultureIds, provinceCultureValues, startIndex, endIndex, amountAdults);
    }

    public ObjectIntMap<String> getReligionsOfHoveredProvince(short x, short y) {
        long provinceId = this.worldManager.getLandProvinceId(x, y);
        Entity province = this.gameContext.getEcsWorld().obtainEntity(provinceId);
        int provinceNameId = Integer.parseInt(province.getName());
        ProvinceStore provinceStore = this.worldManager.getProvinceStore();
        int provinceIndex = provinceStore.getIndexById().get(provinceNameId);
        int amountAdults = provinceStore.getAmountAdults().get(provinceIndex);
        LongList provinceReligionIds = provinceStore.getReligionIds();
        IntList provinceReligionValues = provinceStore.getReligionValues();
        int startIndex = provinceStore.getReligionStarts().get(provinceIndex);
        int endIndex = startIndex + provinceStore.getReligionCounts().get(provinceIndex);

        return this.calculatePercentageDistributionFromProvinceData(provinceReligionIds, provinceReligionValues, startIndex, endIndex, amountAdults);
    }

    public String getColonizerIdOfSelectedProvince() {
        Entity selectedProvince = this.gameContext.getEcsWorld().obtainEntity(this.worldManager.getSelectedProvinceId());
        long countryId = selectedProvince.target(this.gameContext.getEcsConstants().ownedBy());
        return this.worldManager.getColonizerId(countryId);
    }

    public String getColonizerIdOfCountryPlayer() {
        Entity selectedProvince = this.gameContext.getEcsWorld().obtainEntity(this.worldManager.getSelectedProvinceId());
        long countryId = selectedProvince.target(this.gameContext.getEcsConstants().ownedBy());
        return this.worldManager.getColonizerId(countryId);
    }

    public String getColonizerIdOfHoveredProvince(short x, short y) {
        Entity province = this.gameContext.getEcsWorld().obtainEntity(this.worldManager.getLandProvinceId(x, y));
        return this.worldManager.getColonizerId(province.target(this.gameContext.getEcsConstants().ownedBy()));
    }

    public float getResourceGoodsProduction() {
        long selectedProvinceId = this.worldManager.getSelectedProvinceId();
        if(selectedProvinceId == -1) {
            return -1;
        }
        Entity selectedProvince = this.gameContext.getEcsWorld().obtainEntity(selectedProvinceId);
        return this.economyService.getResourceGoodsProduction(Short.parseShort(selectedProvince.getName()));
    }

    public RegionsBuildingsDto prepareRegionsBuildingsDto() {
        return this.economyService.prepareRegionsBuildingsDto();
    }

    public RegionsBuildingsDto prepareRegionsBuildingsDtoSorted(SortType sortType) {
        return this.economyService.prepareRegionsBuildingsDtoSorted(sortType);
    }

    @Override
    public void onNewDay(LocalDate date) {
        this.economyService.hire();
        this.economyService.produce();
    }

    private ObjectIntMap<String> calculatePercentageDistributionFromProvinceData(LongList provinceElementIds, IntList provinceElementValues, int startIndex, int endIndex, int amountAdults) {
        ObjectIntOrderedMap<String> elementPercentages = new ObjectIntOrderedMap<>();
        World ecsWorld = this.gameContext.getEcsWorld();
        int total = 0;
        int biggestElementIndex = -1;
        for(int elementIndex = startIndex; elementIndex < endIndex; elementIndex++) {
            int amount = provinceElementValues.get(elementIndex);
            long elementId = provinceElementIds.get(elementIndex);
            if(biggestElementIndex == -1 || amount > provinceElementValues.get(biggestElementIndex)) {
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
            String biggestElementName = ecsWorld.obtainEntity(provinceElementIds.get(biggestElementIndex)).getName();
            elementPercentages.put(biggestElementName, elementPercentages.get(biggestElementName) + difference);
        }

        this.sortByValueDescending(elementPercentages);

        return elementPercentages;
    }

    private String getPopulationRegionOfSelectedProvince(Map<String, String> localisation) {
        World ecsWorld = this.gameContext.getEcsWorld();
        EcsConstants ecsConstants = this.gameContext.getEcsConstants();

        Entity selectedProvince = ecsWorld.obtainEntity(this.worldManager.getSelectedProvinceId());
        long regionId = selectedProvince.target(ecsConstants.locatedInRegion());
        MutableInt population = new MutableInt(0);

        try (Query query = ecsWorld.query().with(ecsConstants.locatedInRegion(), regionId).build()) {
            query.each(provinceId -> {
                population.increment(this.worldManager.getPopulationAmountOfProvince(provinceId));
            });
        }

        return ValueFormatter.formatValue(population.getValue(), localisation);
    }

    private String getWorkersRegionOfSelectedProvince(Map<String, String> localisation) {
        World ecsWorld = this.gameContext.getEcsWorld();
        EcsConstants ecsConstants = this.gameContext.getEcsConstants();

        Entity selectedProvince = ecsWorld.obtainEntity(this.worldManager.getSelectedProvinceId());
        long regionId = selectedProvince.target(ecsConstants.locatedInRegion());
        MutableInt workers = new MutableInt(0);

        try (Query query = ecsWorld.query().with(ecsConstants.locatedInRegion(), regionId).build()) {
            query.each(provinceId -> {
                workers.increment(this.worldManager.getAmountAdults(provinceId));
            });
        }

        return ValueFormatter.formatValue(workers.getValue(), localisation);
    }

    private List<String> getCountriesCoreOfSelectedProvince() {
        World ecsWorld = this.gameContext.getEcsWorld();
        EcsConstants ecsConstants = this.gameContext.getEcsConstants();

        List<String> countriesCore = new ObjectList<>();
        try (Query query = ecsWorld.query().with(ecsConstants.coreOf(), this.worldManager.getSelectedProvinceId()).build()) {
            query.each(provinceId -> {
                long countryId = ecsWorld.obtainEntity(provinceId).target(ecsConstants.coreOf());
                String countryNameId = ecsWorld.obtainEntity(countryId).getName();
                countriesCore.add(countryNameId);
            });
        }

        return countriesCore;
    }

    private DevelopementBuildingLevelDto getDevelopementBuildingLevel(int provinceId) {
        byte navalBaseLevel = 0;
        byte airBaseLevel = 0;
        byte radarStationLevel = 0;
        byte antiAircraftGunsLevel = 0;

        ProvinceStore provinceStore = this.worldManager.getProvinceStore();
        BuildingStore buildingStore = this.worldManager.getBuildingStore();

        int provinceIndex = provinceStore.getIndexById().get(provinceId);

        int buildingStart = provinceStore.getBuildingStarts().get(provinceIndex);
        int buildingCount = provinceStore.getBuildingCounts().get(provinceIndex);

        for (int i = buildingStart; i < buildingStart + buildingCount; i++) {
            int buildingId = provinceStore.getBuildingIds().get(i);
            int buildingLevel = provinceStore.getBuildingValues().get(i);
            String buildingName = buildingStore.getNames().get(buildingId);

            switch (buildingName) {
                case "naval_base" -> navalBaseLevel = (byte) buildingLevel;
                case "air_base" -> airBaseLevel = (byte) buildingLevel;
                case "radar_station" -> radarStationLevel = (byte) buildingLevel;
                case "anti_air" -> antiAircraftGunsLevel = (byte) buildingLevel;
            }
        }

        return new DevelopementBuildingLevelDto(navalBaseLevel, airBaseLevel, radarStationLevel, antiAircraftGunsLevel);
    }

    private List<String> getProvinceIdsOrderByPopulation(long regionId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        EcsConstants ecsConstants = this.gameContext.getEcsConstants();
        ProvinceStore provinceStore = this.worldManager.getProvinceStore();

        IntList provinceIndices = new IntList();
        try (Query query = ecsWorld.query().with(ecsConstants.locatedInRegion(), regionId).build()) {
            query.each(provinceId -> {
                int provinceIndex = provinceStore.getIndexById().get(Integer.parseInt(ecsWorld.obtainEntity(provinceId).getName()));
                provinceIndices.add(provinceIndex);
            });
        }

        provinceIndices.sort((a, b) -> {
            int populationA = provinceStore.getAmountAdults().get(a);
            int populationB = provinceStore.getAmountAdults().get(b);
            return Integer.compare(populationB, populationA);
        });

        List<String> result = new ObjectList<>();
        for (int provinceIndex = 0; provinceIndex < provinceIndices.size(); provinceIndex++) {
            int id = provinceIndices.get(provinceIndex);
            result.add(String.valueOf(id));
        }

        return result;
    }

    private List<String> getColorBuildingsOrderByLevel(long regionId) {
        RegionStore regionStore = this.worldManager.getRegionStore();
        BuildingStore buildingStore = this.worldManager.getBuildingStore();

        Entity region = this.gameContext.getEcsWorld().obtainEntity(regionId);
        String regionNameId = region.getName();
        int regionIndex = regionStore.getRegionIds().get(regionNameId);
        int regionBuildingStart = regionStore.getBuildingStarts().get(regionIndex);
        int regionBuildingEnd = regionBuildingStart + regionStore.getBuildingCounts().get(regionIndex);

        List<Integer> buildingIndices = new ObjectList<>();

        for (int buildingIndex = regionBuildingStart; buildingIndex < regionBuildingEnd; buildingIndex++) {
            int buildingId = regionStore.getBuildingIds().get(buildingIndex);
            byte buildingType = buildingStore.getTypes().get(buildingId);

            if (buildingType == BuildingType.ECONOMY.getId()) {
                String buildingName = buildingStore.getNames().get(buildingId);
                String color = BuildingUtils.getColor(buildingName);

                if (color != null) {
                    buildingIndices.add(buildingIndex);
                }
            }
        }

        buildingIndices.sort((a, b) -> {
            int levelA = regionStore.getBuildingValues().get(a);
            int levelB = regionStore.getBuildingValues().get(b);
            return Integer.compare(levelB, levelA);
        });

        List<String> colors = new ObjectList<>();
        for (int buildingIndex : buildingIndices) {
            int buildingId = regionStore.getBuildingIds().get(buildingIndex);
            String buildingName = buildingStore.getNames().get(buildingId);
            String color = BuildingUtils.getColor(buildingName);
            colors.add(color);
        }

        return colors;
    }

    private int getNumberIndustry(long regionId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        int industryCount = 0;
        RegionStore regionStore = this.worldManager.getRegionStore();
        BuildingStore buildingStore = this.worldManager.getBuildingStore();

        String regionNameId = ecsWorld.obtainEntity(regionId).getName();
        int regionIndex = regionStore.getRegionIds().get(regionNameId);

        int buildingStart = regionStore.getBuildingStarts().get(regionIndex);
        int buildingEnd = buildingStart + regionStore.getBuildingCounts().get(regionIndex);

        for (int i = buildingStart; i < buildingEnd; i++) {
            int buildingId = regionStore.getBuildingIds().get(i);
            byte buildingType = buildingStore.getTypes().get(buildingId);

            if (buildingType == BuildingType.ECONOMY.getId()) {
                industryCount++;
            }
        }

        return industryCount;
    }

    private List<String> getSpecialBuildingNames(long regionId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        List<String> specialBuildingNames = new ObjectList<>();
        RegionStore regionStore = this.worldManager.getRegionStore();

        String regionNameId = ecsWorld.obtainEntity(regionId).getName();
        int regionIndex = regionStore.getRegionIds().get(regionNameId);

        int buildingStart = regionStore.getBuildingStarts().get(regionIndex);
        int buildingEnd = buildingStart + regionStore.getBuildingCounts().get(regionIndex);

        BuildingStore buildingStore = this.worldManager.getBuildingStore();

        for (int i = buildingStart; i < buildingEnd; i++) {
            int buildingId = regionStore.getBuildingIds().get(i);
            byte buildingType = buildingStore.getTypes().get(buildingId);

            if (buildingType == BuildingType.SPECIAL.getId()) {
                String buildingName = buildingStore.getNames().get(buildingId);
                specialBuildingNames.add(buildingName);
            }
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
        long headOfStateId = country.target(ecsConstants.headOfState());

        long countryColonizerId = country.target(ecsConstants.isColonyOf());
        if(countryColonizerId != 0) {
            Entity colonizerCountry = ecsWorld.obtainEntity(countryColonizerId);
            headOfStateId = colonizerCountry.target(ecsConstants.headOfState());
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

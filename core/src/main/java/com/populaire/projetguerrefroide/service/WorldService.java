package com.populaire.projetguerrefroide.service;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.ObjectIntMap;
import com.github.tommyettinger.ds.ObjectIntOrderedMap;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.adapter.graphics.WgProjection;
import com.populaire.projetguerrefroide.dao.WorldDao;
import com.populaire.projetguerrefroide.dao.impl.WorldDaoImpl;
import com.populaire.projetguerrefroide.dto.CountryDto;
import com.populaire.projetguerrefroide.dto.CountrySummaryDto;
import com.populaire.projetguerrefroide.dto.ProvinceDto;
import com.populaire.projetguerrefroide.economy.building.BuildingType;
import com.populaire.projetguerrefroide.map.RegionStore;
import com.populaire.projetguerrefroide.economy.building.BuildingStore;
import com.populaire.projetguerrefroide.dto.DevelopementBuildingLevelDto;
import com.populaire.projetguerrefroide.politics.AllianceType;
import com.populaire.projetguerrefroide.politics.Minister;
import com.populaire.projetguerrefroide.map.*;
import com.populaire.projetguerrefroide.util.BuildingUtils;
import com.populaire.projetguerrefroide.util.ValueFormatter;

import java.util.List;
import java.util.Map;

public class WorldService {
    private final AsyncExecutor asyncExecutor;
    private final WorldDao worldDao;
    private World world;
    private final ObjectIntOrderedMap<String> elementPercentages;

    public WorldService() {
        this.asyncExecutor = new AsyncExecutor(2);
        this.worldDao = new WorldDaoImpl();
        this.elementPercentages = new ObjectIntOrderedMap<>();
    }

    public void createWorld(GameContext gameContext) {
        this.world = this.worldDao.createWorldThreadSafe(gameContext);
    }

    public AsyncExecutor getAsyncExecutor() {
        return this.asyncExecutor;
    }

    public void renderWorld(WgProjection projection, OrthographicCamera cam, float time) {
        this.world.render(projection, cam, time);
    }

    public boolean selectProvince(short x, short y) {
        return this.world.selectProvince(x, y);
    }

    public boolean isProvinceSelected() {
        return this.world.getSelectedProvince() != null;
    }

    public boolean setCountryPlayer() {
        return this.world.setCountryPlayer();
    }

    public boolean hoverProvince(short x, short y) {
        return this.world.getProvince(x, y) != null;
    }

    public short getProvinceId(short x, short y) {
        return this.world.getProvince(x, y).getId();
    }

    public MapMode getMapMode() {
        return this.world.getMapMode();
    }

    public String getCountryIdOfHoveredProvince(short x, short y) {
        return this.world.getProvince(x, y).getCountryOwner().getId();
    }

    public int getPositionOfCapitalOfSelectedCountry() {
        return this.world.getSelectedProvince().getCountryOwner().getCapital().getPosition("default");
    }

    public String getCountryIdPlayer() {
        return this.world.getCountryPlayer().getId();
    }

    public short getNumberOfProvinces() {
        return this.world.getNumberOfProvinces();
    }

    public int getRankingOfSelectedCountry() {
        return 0;
    }

    public CountrySummaryDto prepareCountrySummaryDto(Map<String, String> localisation) {
        Country selectedCountry = this.world.getSelectedProvince().getCountryOwner();
        Minister headOfState = this.getHeadOfState(selectedCountry);
        String portraitNameFile = "admin_type";
        if(headOfState.getImageNameFile() != null) {
            portraitNameFile = headOfState.getImageNameFile();
        }
        String population = ValueFormatter.formatValue(this.world.getPopulationAmount(selectedCountry), localisation);
        List<String> allies = this.getAlliesOfSelectedCountry(selectedCountry);

        return new CountrySummaryDto(selectedCountry.getId(), population, selectedCountry.getGovernment().getName(), portraitNameFile, headOfState.getName(), this.world.getColonizerId(selectedCountry), allies);
    }

    public CountryDto prepareCountryDto(Map<String, String> localisation) {
        Country selectedCountry = this.world.getSelectedProvince().getCountryOwner();
        String population = ValueFormatter.formatValue(this.world.getPopulationAmount(selectedCountry), localisation);
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
        LandProvince selectedProvince = this.world.getSelectedProvince();
        Region region = selectedProvince.getRegion();
        String provinceId = String.valueOf(selectedProvince.getId());
        String regionId = region.getId();
        String terrainImage = selectedProvince.getTerrain().getName();
        String resourceImage = this.world.getResourceGoodName(selectedProvince);
        String populationRegion = this.getPopulationRegionOfSelectedProvince(localisation);
        String workersRegion = this.getWorkersRegionOfSelectedProvince(localisation);
        String populationProvince = ValueFormatter.formatValue(this.world.getPopulationAmount(selectedProvince), localisation);
        int developmentIndexRegion = 0;
        int incomeRegion = 0;
        int numberIndustryRegion = this.getNumberIndustry(region);
        String countryId = selectedProvince.getCountryOwner().getId();
        String colonizerId = this.world.getColonizerId(selectedProvince.getCountryOwner());
        List<String> flagCountriesCore = this.getCountriesCoreOfSelectedProvince();
        List<String> provinceIdsRegion = this.getProvinceIdsOrderByPopulation(region);
        DevelopementBuildingLevelDto developmentBuildingLevel = this.getDevelopementBuildingLevel(region);
        List<String> specialBuildings = this.getSpecialBuildingNames(region);
        List<String> colorsBuilding = this.getColorBuildingsOrderByLevel(region);

        return new ProvinceDto(provinceId, regionId, terrainImage, resourceImage, populationRegion, workersRegion, developmentIndexRegion, incomeRegion, numberIndustryRegion, countryId, colonizerId, flagCountriesCore, 0f, 0, 0, populationProvince, 0f, 0f, provinceIdsRegion, developmentBuildingLevel, specialBuildings, colorsBuilding);
    }

    public void changeMapMode(String mapMode) {
        this.world.changeMapMode(mapMode);
    }

    public ObjectIntMap<String> getCulturesOfHoveredProvince(short x, short y) {
        LandProvince province = this.world.getProvince(x, y);
        ProvinceStore provinceStore = this.world.getProvinceStore();
        int provinceIndex = provinceStore.getIndexById().get(province.getId());
        int amountAdults = provinceStore.getAmountAdults().get(provinceIndex);
        IntList provinceCultureIds = provinceStore.getCultureIds();
        IntList provinceCultureValues = provinceStore.getCultureValues();
        int startIndex = provinceStore.getCultureStarts().get(provinceIndex);
        int endIndex = startIndex + provinceStore.getCultureCounts().get(provinceIndex);
        List<String> cultureNames = this.world.getNationalIdeas().getCultureStore().getNames();
        return this.calculatePercentageDistributionFromProvinceData(provinceCultureIds, provinceCultureValues, startIndex, endIndex, cultureNames, amountAdults);
    }

    public ObjectIntMap<String> getReligionsOfHoveredProvince(short x, short y) {
        LandProvince province = this.world.getProvince(x, y);
        ProvinceStore provinceStore = this.world.getProvinceStore();
        int provinceIndex = provinceStore.getIndexById().get(province.getId());
        int amountAdults = provinceStore.getAmountAdults().get(provinceIndex);
        IntList provinceReligionIds = provinceStore.getReligionIds();
        IntList provinceReligionValues = provinceStore.getReligionValues();
        int startIndex = provinceStore.getReligionStarts().get(provinceIndex);
        int endIndex = startIndex + provinceStore.getReligionCounts().get(provinceIndex);
        List<String> religionNames = this.world.getNationalIdeas().getReligionStore().getNames();

        return this.calculatePercentageDistributionFromProvinceData(provinceReligionIds, provinceReligionValues, startIndex, endIndex, religionNames, amountAdults);
    }

    public String getColonizerIdOfSelectedProvince() {
        Country country = this.world.getSelectedProvince().getCountryOwner();
        return this.world.getColonizerId(country);
    }

    public String getColonizerIdOfCountryPlayer() {
        Country country = this.world.getCountryPlayer();
        return this.world.getColonizerId(country);
    }

    public String getColonizerIdOfHoveredProvince(short x, short y) {
        LandProvince province = this.world.getProvince(x, y);
        Country country = province.getCountryOwner();
        return this.world.getColonizerId(country);
    }

    private ObjectIntMap<String> calculatePercentageDistributionFromProvinceData(IntList provinceElementIds, IntList provinceElementValues, int startIndex, int endIndex, List<String> elementNames, int amountAdults) {
        this.elementPercentages.clear();
        int total = 0;
        int biggestElementIndex = -1;
        for(int elementIndex = startIndex; elementIndex < endIndex; elementIndex++) {
            int amount = provinceElementValues.get(elementIndex);
            int elementId = provinceElementIds.get(elementIndex);
            if(biggestElementIndex == -1 || amount > provinceElementValues.get(biggestElementIndex)) {
                biggestElementIndex = elementIndex;
            }
            if(amountAdults != 0) {
                int percentage = (int) ((amount / (float) amountAdults) * 100);
                total += percentage;
                this.elementPercentages.put(elementNames.get(elementId), percentage);
            } else {
                this.elementPercentages.put(elementNames.get(elementId), 0);
            }
        }

        if(total != 100 && biggestElementIndex != -1) {
            int difference = 100 - total;
            String biggestElementName = elementNames.get(provinceElementIds.get(biggestElementIndex));
            this.elementPercentages.put(biggestElementName, this.elementPercentages.get(biggestElementName) + difference);
        }

        this.sortByValueDescending(this.elementPercentages);

        return this.elementPercentages;
    }

    private String getPopulationRegionOfSelectedProvince(Map<String, String> localisation) {
        int population = 0;
        for(LandProvince province : this.world.getSelectedProvince().getRegion().getProvinces()) {
            population += this.world.getPopulationAmount(province);
        }

        return ValueFormatter.formatValue(population, localisation);
    }

    private String getWorkersRegionOfSelectedProvince(Map<String, String> localisation) {
        int workers = 0;
        for(LandProvince province : this.world.getSelectedProvince().getRegion().getProvinces()) {
            workers += this.world.getAmountAdults(province);
        }

        return ValueFormatter.formatValue(workers, localisation);
    }

    private List<String> getCountriesCoreOfSelectedProvince() {
        List<String> countriesCore = new ObjectList<>();
        for(Country country : this.world.getSelectedProvince().getCountriesCore()) {
            countriesCore.add(country.getId());
        }

        return countriesCore;
    }

    private DevelopementBuildingLevelDto getDevelopementBuildingLevel(Region region) {
        byte navalBaseLevel = 0;
        byte airBaseLevel = 0;
        byte radarStationLevel = 0;
        byte antiAircraftGunsLevel = 0;

        RegionStore regionStore = this.world.getRegionStore();
        BuildingStore buildingStore = this.world.getBuildingStore();

        int regionIndex = regionStore.getRegionIds().get(region.getId());

        int buildingStart = regionStore.getBuildingStarts().get(regionIndex);
        int buildingEnd = buildingStart + regionStore.getBuildingCounts().get(regionIndex);

        for (int i = buildingStart; i < buildingEnd; i++) {
            int buildingId = regionStore.getBuildingIds().get(i);
            int buildingLevel = regionStore.getBuildingValues().get(i);
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

    private List<String> getProvinceIdsOrderByPopulation(Region region) {
        ProvinceStore provinceStore = this.world.getProvinceStore();

        IntList provinceIndices = new IntList();
        for (LandProvince province : region.getProvinces()) {
            int provinceIndex = provinceStore.getIndexById().get(province.getId());
            provinceIndices.add(provinceIndex);
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

    private List<String> getColorBuildingsOrderByLevel(Region region) {
        RegionStore regionStore = this.world.getRegionStore();
        BuildingStore buildingStore = this.world.getBuildingStore();

        int regionId = regionStore.getRegionIds().get(region.getId());
        int regionBuildingStart = regionStore.getBuildingStarts().get(regionId);
        int regionBuildingEnd = regionBuildingStart + regionStore.getBuildingCounts().get(regionId);

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

    private int getNumberIndustry(Region region) {
        int industryCount = 0;
        RegionStore regionStore = this.world.getRegionStore();
        BuildingStore buildingStore = this.world.getBuildingStore();

        int regionIndex = regionStore.getRegionIds().get(region.getId());

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

    private List<String> getSpecialBuildingNames(Region region) {
        List<String> specialBuildingNames = new ObjectList<>();
        RegionStore regionStore = this.world.getRegionStore();

        int regionId = regionStore.getRegionIds().get(region.getId());

        int buildingStart = regionStore.getBuildingStarts().get(regionId);
        int buildingEnd = buildingStart + regionStore.getBuildingCounts().get(regionId);

        BuildingStore buildingStore = this.world.getBuildingStore();

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

    private List<String> getAlliesOfSelectedCountry(Country country) {
        List<String> allies = new ObjectList<>();
        if(country.getAlliances() == null) {
            return allies;
        }

        for(Map.Entry<Country, AllianceType> alliance : country.getAlliances().entrySet()) {
            if(alliance.getValue() != AllianceType.COLONIZER) {
                allies.add(alliance.getKey().getId());
            }
        }
        return allies;
    }

    public Minister getHeadOfState(Country country) {
        Minister headOfState = null;
        if(country.getHeadOfStateId() != -1) {
            headOfState = this.world.getPolitics().getMinister(country.getHeadOfStateId());
        }

        if(country.getAlliances() != null) {
            for (Map.Entry<Country, AllianceType> alliance : country.getAlliances().entrySet()) {
                if (alliance.getValue() == AllianceType.COLONY) {
                    headOfState = this.world.getPolitics().getMinister(alliance.getKey().getHeadOfStateId());
                }
            }
        }

        return headOfState;
    }

    public void dispose() {
        this.world.dispose();
        this.asyncExecutor.dispose();
    }
}

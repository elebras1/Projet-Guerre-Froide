package com.populaire.projetguerrefroide.service;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.github.tommyettinger.ds.ObjectIntMap;
import com.github.tommyettinger.ds.ObjectIntOrderedMap;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.dao.WorldDao;
import com.populaire.projetguerrefroide.dao.WorldDaoImplDsl;
import com.populaire.projetguerrefroide.dto.CountryDto;
import com.populaire.projetguerrefroide.dto.CountrySummaryDto;
import com.populaire.projetguerrefroide.dto.ProvinceDto;
import com.populaire.projetguerrefroide.economy.building.Building;
import com.populaire.projetguerrefroide.economy.building.EconomyBuilding;
import com.populaire.projetguerrefroide.economy.building.SpecialBuilding;
import com.populaire.projetguerrefroide.entity.DevelopementBuildingLevel;
import com.populaire.projetguerrefroide.entity.GameEntities;
import com.populaire.projetguerrefroide.entity.Minister;
import com.populaire.projetguerrefroide.map.*;
import com.populaire.projetguerrefroide.util.BuildingUtils;
import com.populaire.projetguerrefroide.util.Named;
import com.populaire.projetguerrefroide.util.ValueFormatter;

import java.util.List;
import java.util.Map;

public class WorldService {
    private final AsyncExecutor asyncExecutor;
    private final WorldDao worldDao;
    private GameEntities gameEntities;
    private World world;
    private final ObjectIntOrderedMap<String> elementPercentages;

    public WorldService() {
        this.asyncExecutor = new AsyncExecutor(2);
        this.worldDao = new WorldDaoImplDsl("1946.1.1");
        this.elementPercentages = new ObjectIntOrderedMap<>();
    }

    public void createWorld(GameContext gameContext) {
        this.world = this.worldDao.createWorldThreadSafe(this.getGameEntities(), gameContext);
    }

    public AsyncExecutor getAsyncExecutor() {
        return this.asyncExecutor;
    }

    public GameEntities getGameEntities() {
        if(this.gameEntities == null) {
            this.gameEntities = this.worldDao.createGameEntities();
        }

        return this.gameEntities;
    }

    public void renderWorld(SpriteBatch batch, OrthographicCamera cam, float time) {
        this.world.render(batch, cam, time);
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

    public String getCountryNameOfHoveredProvince(short x, short y) {
        return this.world.getProvince(x, y).getCountryOwner().getName();
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
        Minister headOfState = selectedCountry.getHeadOfState();
        String portraitNameFile = "admin_type";
        if(headOfState.getImageNameFile() != null) {
            portraitNameFile = headOfState.getImageNameFile();
        }
        String population = ValueFormatter.formatValue(selectedCountry.getPopulationAmount(), localisation);

        return new CountrySummaryDto(selectedCountry.getName(), selectedCountry.getId(), population, selectedCountry.getGovernment().getName(), portraitNameFile, headOfState.getName());
    }

    public CountryDto prepareCountryDto(Map<String, String> localisation) {
        Country selectedCountry = this.world.getSelectedProvince().getCountryOwner();
        String population = ValueFormatter.formatValue(selectedCountry.getPopulationAmount(), localisation);
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
        String resourceImage = selectedProvince.getResourceGood() != null ? selectedProvince.getResourceGood().getName() : null;
        String populationRegion = this.getPopulationRegionOfSelectedProvince(localisation);
        String workersRegion = this.getWorkersRegionOfSelectedProvince(localisation);
        String populationProvince = ValueFormatter.formatValue(selectedProvince.getPopulation().getAmount(), localisation);
        int developmentIndexRegion = 0;
        int incomeRegion = 0;
        int numberIndustryRegion = this.getNumberIndustryOfRegion(region);
        String flagImage = selectedProvince.getCountryOwner().getId();
        List<String> flagCountriesCore = this.getCountriesCoreOfSelectedProvince();
        List<String> provinceIdsRegion = this.getProvinceIdsOfRegionOrderByPopulation(region);
        DevelopementBuildingLevel developmentBuildingLevel = this.getDevelopementBuildingLevelOfRegion(region);
        List<String> specialBuildings = this.getSpecialBuildingNamesOfRegion(region);
        List<String> colorsBuilding = this.getColorBuildingsOfRegionOrderByLevel(region);

        return new ProvinceDto(provinceId, regionId, terrainImage, resourceImage, populationRegion, workersRegion, developmentIndexRegion, incomeRegion, numberIndustryRegion, flagImage, flagCountriesCore, 0f, 0, 0, populationProvince, 0f, 0f, provinceIdsRegion, developmentBuildingLevel, specialBuildings, colorsBuilding);
    }

    public void changeMapMode(String mapMode) {
        this.world.changeMapMode(mapMode);
    }

    public ObjectIntMap<String> getCulturesOfHoveredProvince(short x, short y) {
        LandProvince province = this.world.getProvince(x, y);
        int amountAdults = province.getPopulation().getAmountAdults();
        return this.calculatePercentageDistributionFromProvinceData(province.getPopulation().getCultures(), amountAdults);
    }

    public ObjectIntMap<String> getReligionsOfHoveredProvince(short x, short y) {
        LandProvince province = this.world.getProvince(x, y);
        int amountAdults = province.getPopulation().getAmountAdults();
        return this.calculatePercentageDistributionFromProvinceData(province.getPopulation().getReligions(), amountAdults);
    }

    private <E extends Named> ObjectIntMap<String> calculatePercentageDistributionFromProvinceData(ObjectIntMap<E> elements, int amountAdults) {
        this.elementPercentages.clear();
        int total = 0;
        E biggestElement = null;
        for(E element : elements.keySet()) {
            int amount = elements.get(element);
            if(biggestElement == null || amount > elements.get(biggestElement)) {
                biggestElement = element;
            }
            if(amountAdults != 0) {
                int percentage = (int) ((amount / (float) amountAdults) * 100);
                total += percentage;
                this.elementPercentages.put(element.getName(), percentage);
            } else {
                this.elementPercentages.put(element.getName(), 0);
            }
        }

        if(total != 100 && biggestElement != null) {
            int difference = 100 - total;
            this.elementPercentages.put(biggestElement.getName(), this.elementPercentages.get(biggestElement.getName()) + difference);
        }

        this.sortByValueDescending(this.elementPercentages);

        return this.elementPercentages;
    }

    private String getPopulationRegionOfSelectedProvince(Map<String, String> localisation) {
        int population = 0;
        for(LandProvince province : this.world.getSelectedProvince().getRegion().getProvinces()) {
            population += province.getPopulation().getAmount();
        }

        return ValueFormatter.formatValue(population, localisation);
    }

    private String getWorkersRegionOfSelectedProvince(Map<String, String> localisation) {
        int workers = 0;
        for(LandProvince province : this.world.getSelectedProvince().getRegion().getProvinces()) {
            workers += province.getPopulation().getAmountAdults();
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

    private DevelopementBuildingLevel getDevelopementBuildingLevelOfRegion(Region region) {
        byte navalBaseLevel = 0;
        byte airBaseLevel = 0;
        byte radarStationLevel = 0;
        byte antiAircraftGunsLevel = 0;
        for(LandProvince province : region.getProvinces()) {
            for(Building building : province.getBuildings().keySet()) {
                switch (building.getName()) {
                    case "naval_base" -> navalBaseLevel = (byte) province.getBuildings().get(building);
                    case "air_base" -> airBaseLevel = (byte) province.getBuildings().get(building);
                    case "radar_station" -> radarStationLevel = (byte) province.getBuildings().get(building);
                    case "anti_air" -> antiAircraftGunsLevel = (byte) province.getBuildings().get(building);
                }
            }
        }

        return new DevelopementBuildingLevel(navalBaseLevel, airBaseLevel, radarStationLevel, antiAircraftGunsLevel);
    }

    private List<String> getProvinceIdsOfRegionOrderByPopulation(Region region) {
        List<LandProvince> provinces = new ObjectList<>(region.getProvinces());

        provinces.sort((a, b) -> Integer.compare(b.getPopulation().getAmount(), a.getPopulation().getAmount()));

        List<String> provinceIds = new ObjectList<>();
        for (LandProvince province : provinces) {
            provinceIds.add(String.valueOf(province.getId()));
        }

        return provinceIds;
    }

    private List<String> getColorBuildingsOfRegionOrderByLevel(Region region) {
        List<Building> buildings = new ObjectList<>(region.getBuildings().keySet());

        buildings.sort((a, b) -> Integer.compare(region.getBuildings().get(b), region.getBuildings().get(a)));

        List<String> colors = new ObjectList<>();
        for (Building building : buildings) {
            if (building instanceof EconomyBuilding) {
                String color = BuildingUtils.getColor(building.getName());
                if (color != null) {
                    colors.add(color);
                }
            }
        }

        return colors;
    }

    private int getNumberIndustryOfRegion(Region region) {
        int industryRegion = 0;
        for(Building building : region.getBuildings().keySet()) {
            if(building instanceof EconomyBuilding) {
                industryRegion++;
            }
        }

        return industryRegion;
    }

    private List<String> getSpecialBuildingNamesOfRegion(Region region) {
        List<String> specialBuildingNames = new ObjectList<>();
        for (Building building : region.getBuildings().keySet()) {
            if (building instanceof SpecialBuilding) {
                specialBuildingNames.add(building.getName());
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

    public void dispose() {
        this.world.dispose();
        this.asyncExecutor.dispose();
    }
}

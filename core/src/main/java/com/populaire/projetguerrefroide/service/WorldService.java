package com.populaire.projetguerrefroide.service;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.github.tommyettinger.ds.ObjectIntMap;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.dao.WorldDao;
import com.populaire.projetguerrefroide.economy.good.ResourceGood;
import com.populaire.projetguerrefroide.entity.GameEntities;
import com.populaire.projetguerrefroide.entity.Government;
import com.populaire.projetguerrefroide.entity.Minister;
import com.populaire.projetguerrefroide.map.Country;
import com.populaire.projetguerrefroide.map.LandProvince;
import com.populaire.projetguerrefroide.map.MapMode;
import com.populaire.projetguerrefroide.map.World;
import com.populaire.projetguerrefroide.util.Named;
import com.populaire.projetguerrefroide.util.ValueFormatter;

import java.util.List;
import java.util.Map;

public class WorldService {
    private final AsyncExecutor asyncExecutor;
    private final WorldDao worldDao;
    private GameEntities gameEntities;
    private World world;
    private final ObjectIntMap<String> elementPercentages;
    private final List<String> elements;

    public WorldService() {
        this.asyncExecutor = new AsyncExecutor(2);;
        this.worldDao = new WorldDao();
        this.elementPercentages = new ObjectIntMap<>();
        this.elements = new ObjectList<>();
    }

    public void createWorld(GameContext gameContext) {
        this.world = this.worldDao.createWorldThreadSafe(this.getGameEntities(), this.asyncExecutor, gameContext);
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

    public String getSelectedProvinceId() {
        return String.valueOf(this.world.getSelectedProvince().getId());
    }

    public String getTerrainOfSelectedProvince() {
        return this.world.getSelectedProvince().getTerrain().getName();
    }

    public String getRegionIdOfSelectedProvince() {
        return this.world.getSelectedProvince().getRegion().getId();
    }

    public String getResourceOfSelectedProvince() {
        ResourceGood resourceGood = this.world.getSelectedProvince().getResourceGood();
        if(resourceGood != null) {
            return resourceGood.getName();
        }

        return null;
    }

    public String getPopulationRegionOfSelectedProvince(Map<String, String> localisation) {
        int population = 0;
        for(LandProvince province : this.world.getSelectedProvince().getRegion().getProvinces()) {
            population += province.getPopulation().getAmount();
        }

        return ValueFormatter.formatValue(population, localisation);
    }

    public String getWorkersRegionOfSelectedProvince(Map<String, String> localisation) {
        int workers = 0;
        for(LandProvince province : this.world.getSelectedProvince().getRegion().getProvinces()) {
            workers += province.getPopulation().getAmountAdults();
        }

        return ValueFormatter.formatValue(workers, localisation);
    }

    public int getNumberIndustryRegionOfSelectedProvince() {
        return this.world.getSelectedProvince().getRegion().getBuildings().size();
    }

    public float getResourceProducedOfSelectedProvince() {
        return 0f;
    }

    public int getInfrastructureValueOfSelectedProvince() {
        return 0;
    }

    public float getGuerillaValueOfSelectedProvince() {
        return 0f;
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

    public String getCountryIdOfSelectedProvince() {
        return this.world.getSelectedProvince().getCountryOwner().getId();
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

    public List<String> getCountriesCoreOfSelectedProvince() {
        this.elements.clear();
        for(Country country : this.world.getSelectedProvince().getCountriesCore()) {
            this.elements.add(country.getId());
        }

        return this.elements;
    }

    public <E extends Named> ObjectIntMap<String> calculatePercentageDistributionFromProvinceData(ObjectIntMap<E> elements, int amountAdults) {
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

        return this.elementPercentages;
    }

    public int getPopulationAmountOfHoveredProvince(short x, short y) {
        return this.world.getProvince(x, y).getPopulation().getAmount();
    }

    public String getIdOfSelectedCountry() {
        return this.world.getSelectedProvince().getCountryOwner().getId();
    }

    public String getNameOfSelectedCountry() {
        return this.world.getSelectedProvince().getCountryOwner().getName();
    }

    public int getPopulationAmountOfSelectedCountry() {
        return this.world.getSelectedProvince().getCountryOwner().getPopulationAmount();
    }

    public Government getGovernmentOfSelectedCountry() {
        return this.world.getSelectedProvince().getCountryOwner().getGovernment();
    }

    public Minister getHeadOfStateOfSelectedCountry() {
        return this.world.getSelectedProvince().getCountryOwner().getHeadOfState();
    }

    public Minister getHeadOfGovernmentOfSelectedCountry() {
        return this.world.getSelectedProvince().getCountryOwner().getHeadOfGovernment();
    }

    public String getCountryIdPlayer() {
        return this.world.getCountryPlayer().getId();
    }

    public short getNumberOfProvinces() {
        return this.world.getNumberOfProvinces();
    }

    public void changeMapMode(String mapMode) {
        this.world.changeMapMode(mapMode);
    }

    public void dispose() {
        this.world.dispose();
        this.asyncExecutor.dispose();
    }
}

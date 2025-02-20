package com.populaire.projetguerrefroide.service;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.github.tommyettinger.ds.ObjectIntMap;
import com.populaire.projetguerrefroide.data.DataManager;
import com.populaire.projetguerrefroide.entity.GameEntities;
import com.populaire.projetguerrefroide.entity.Government;
import com.populaire.projetguerrefroide.entity.Minister;
import com.populaire.projetguerrefroide.map.LandProvince;
import com.populaire.projetguerrefroide.map.MapMode;
import com.populaire.projetguerrefroide.map.World;
import com.populaire.projetguerrefroide.util.Named;

public class WorldService {
    private final AsyncExecutor asyncExecutor;
    private final DataManager dataManager;
    private GameEntities gameEntities;
    private World world;
    private final ObjectIntMap<String> elementPercentages;

    public WorldService() {
        this.asyncExecutor = new AsyncExecutor(2);;
        this.dataManager = new DataManager();
        this.elementPercentages = new ObjectIntMap<>();
    }

    public void createWorld() {
        this.world = this.dataManager.createWorldThreadSafe(this.getGameEntities(), this.asyncExecutor);
    }

    public AsyncExecutor getAsyncExecutor() {
        return this.asyncExecutor;
    }

    public GameEntities getGameEntities() {
        if(this.gameEntities == null) {
            this.gameEntities = this.dataManager.createGameEntities();
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

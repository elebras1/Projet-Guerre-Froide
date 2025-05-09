package com.populaire.projetguerrefroide.service;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.github.tommyettinger.ds.ObjectIntMap;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.dao.WorldDao;
import com.populaire.projetguerrefroide.dto.CountrySummaryDto;
import com.populaire.projetguerrefroide.dto.ProvinceDto;
import com.populaire.projetguerrefroide.entity.GameEntities;
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

    public WorldService() {
        this.asyncExecutor = new AsyncExecutor(2);
        this.worldDao = new WorldDao();
        this.elementPercentages = new ObjectIntMap<>();
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

    public ProvinceDto prepareProvinceDto(Map<String, String> localisation) {
        LandProvince selectedProvince = this.world.getSelectedProvince();
        String provinceId = String.valueOf(selectedProvince.getId());
        String regionId = selectedProvince.getRegion().getId();
        String terrainImage = selectedProvince.getTerrain().getName();
        String resourceImage = selectedProvince.getResourceGood() != null ? selectedProvince.getResourceGood().getName() : null;
        String populationRegion = this.getPopulationRegionOfSelectedProvince(localisation);
        String workersRegion = this.getWorkersRegionOfSelectedProvince(localisation);
        int developmentIndexRegion = 0;
        int incomeRegion = 0;
        int industryRegion = 0;
        String flagImage = selectedProvince.getCountryOwner().getId();
        List<String> flagCountriesCore = this.getCountriesCoreOfSelectedProvince();

        return new ProvinceDto(provinceId, regionId, terrainImage, resourceImage, populationRegion, workersRegion, developmentIndexRegion, incomeRegion, industryRegion, flagImage, flagCountriesCore, 0f, 0, 0);
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

    public void dispose() {
        this.world.dispose();
        this.asyncExecutor.dispose();
    }
}

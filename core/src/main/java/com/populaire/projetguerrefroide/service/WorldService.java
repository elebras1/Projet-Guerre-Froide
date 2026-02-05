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

public class WorldService {
    private final GameContext gameContext;
    private final WorldDao worldDao;
    private final QueryRepository queryRepository;
    private final BuildingService buildingService;
    private final EconomyService economyService;
    private final RegionService regionService;
    private final CountryService countryService;
    private final ProvinceService provinceService;
    private MapService mapService;

    public WorldService(GameContext gameContext, QueryRepository queryRepository, BuildingService buildingService, EconomyService economyService, RegionService regionService, CountryService countryService, ProvinceService provinceService) {
        this.gameContext = gameContext;
        this.worldDao = new WorldDaoImpl();
        this.queryRepository = queryRepository;
        this.buildingService = buildingService;
        this.economyService = economyService;
        this.regionService = regionService;
        this.countryService = countryService;
        this.provinceService = provinceService;
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

    public String getProvinceNameId(int x, int y) {
        long provinceId = this.mapService.getLandProvinceId(x, y);
        return this.provinceService.getName(provinceId);
    }

    public MapMode getMapMode() {
        return this.mapService.getMapMode();
    }

    public String getCountryNameIdOfHoveredProvince(int x, int y) {
        long provinceId = this.mapService.getLandProvinceId(x, y);
        return this.provinceService.getCountryNameId(provinceId);
    }

    public Position getCapitalPositionOfSelectedCountry() {
        World ecsWorld = this.gameContext.getEcsWorld();
        long selectedProvinceId = this.mapService.getSelectedProvinceId();
        if(selectedProvinceId == -1) {
            return null;
        }
        Entity selectedProvince = ecsWorld.obtainEntity(selectedProvinceId);
        Province selectedProvinceData = selectedProvince.get(Province.class);
        return this.countryService.getCapitalPosition(selectedProvinceData.ownerId());
    }

    public String getCountryPlayerNameId() {
        return this.countryService.getName(this.mapService.getPlayerCountryId());
    }

    public int getNumberOfProvinces() {
        return this.mapService.getNumberOfProvinces();
    }

    public CountrySummaryDto buildCountrySummary() {
        World ecsWorld = this.gameContext.getEcsWorld();
        long selectedProvinceId = this.mapService.getSelectedProvinceId();
        if(selectedProvinceId == -1) {
            return null;
        }
        Entity selectedProvince = ecsWorld.obtainEntity(selectedProvinceId);
        Province selectedProvinceData = selectedProvince.get(Province.class);
        return this.countryService.buildSummary(selectedProvinceData.ownerId());
    }

    public CountryDto buildCountryDetails() {
        World ecsWorld = this.gameContext.getEcsWorld();
        long selectedProvinceId = this.mapService.getSelectedProvinceId();
        if(selectedProvinceId == -1) {
            return null;
        }
        Entity selectedProvince = ecsWorld.obtainEntity(selectedProvinceId);
        Province selectedProvinceData = selectedProvince.get(Province.class);
        return this.countryService.buildDetails(selectedProvinceData.ownerId());
    }

    public ProvinceDto buildProvinceDetails() {
        long selectedProvinceId = this.mapService.getSelectedProvinceId();
        if(selectedProvinceId == -1) {
            return null;
        }
        return this.provinceService.buildDetails(selectedProvinceId);
    }

    public BuildingDto buildBuildingDetails(long buildingId) {
        return this.buildingService.buildDetails(buildingId);
    }

    public void changeMapMode(String mapMode) {
        this.mapService.changeMapMode(mapMode);
    }

    public ObjectIntMap<String> getCulturesOfHoveredProvince(int x, int y) {
        long provinceId = this.mapService.getLandProvinceId(x, y);
        return this.provinceService.getCultures(provinceId);
    }

    public ObjectIntMap<String> getReligionsOfHoveredProvince(int x, int y) {
        long provinceId = this.mapService.getLandProvinceId(x, y);
        return this.provinceService.getReligions(provinceId);
    }

    public String getColonizerNameIdOfSelectedProvince() {
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
        return this.provinceService.getResourceGatheringProduction(selectedProvince.getName());
    }

    public RegionsBuildingsDto prepareRegionsBuildingsDto(SortType sortType) {
        return this.countryService.buildRegionsBuildings(this.mapService.getPlayerCountryId(), sortType);
    }

    public String getColonizerId(long countryId) {
        return this.countryService.getColonizerNameId(countryId);
    }

    public void dispose() {
        this.mapService.dispose();
        this.queryRepository.dispose();
    }
}

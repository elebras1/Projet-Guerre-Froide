package com.populaire.projetguerrefroide.service;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.populaire.projetguerrefroide.data.DataManager;
import com.populaire.projetguerrefroide.entity.Government;
import com.populaire.projetguerrefroide.entity.Minister;
import com.populaire.projetguerrefroide.map.World;

public class WorldService {
    private final DataManager dataManager;
    private World world;

    public WorldService() {
        this.dataManager = new DataManager();
    }

    public void createWorld() {
        this.world = this.dataManager.createWorldThreadSafe();
    }

    public void renderWorld(SpriteBatch batch, OrthographicCamera cam, float time) {
        this.world.render(batch, cam, time);
    }

    public void selectProvince(short x, short y) {
        this.world.selectProvince(x, y);
    }

    public boolean isProvinceSelected() {
        return this.world.getSelectedProvince() != null;
    }

    public boolean hoverProvince(short x, short y) {
        return this.world.getProvince(x, y) != null;
    }

    public short getProvinceId(short x, short y) {
        return this.world.getProvince(x, y).getId();
    }

    public String getCountryIdOfHoveredProvince(short x, short y) {
        return this.world.getProvince(x, y).getCountryOwner().getId();
    }

    public String getCountryNameOfHoveredProvince(short x, short y) {
        return this.world.getProvince(x, y).getCountryOwner().getName();
    }

    public String getIdOfSelectedCountry() {
        return this.world.getSelectedProvince().getCountryOwner().getId();
    }

    public String getNameOfSelectedCountry() {
        return this.world.getSelectedProvince().getCountryOwner().getName();
    }

    public int getPopulationSizeOfSelectedCountry() {
        return this.world.getSelectedProvince().getCountryOwner().getPopulationSize();
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
}

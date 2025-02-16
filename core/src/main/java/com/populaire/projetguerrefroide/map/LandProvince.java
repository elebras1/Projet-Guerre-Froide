package com.populaire.projetguerrefroide.map;

import com.github.tommyettinger.ds.IntSet;
import com.github.tommyettinger.ds.ObjectIntMap;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.economy.building.Building;
import com.populaire.projetguerrefroide.economy.good.Good;
import com.populaire.projetguerrefroide.economy.good.ResourceGood;
import com.populaire.projetguerrefroide.economy.population.Population;
import com.populaire.projetguerrefroide.entity.Terrain;

import java.util.*;

public class LandProvince implements Province {
    private int color;
    private short id;
    private Country countryOwner;
    private Country countryController;
    private Region region;
    private Continent continent;
    private Population population;
    private final Terrain terrain;
    private final List<Country> countriesCore;
    private final ResourceGood resourceGood;
    private final ObjectIntMap<Building> buildings;
    private final List<Province> adjacentProvinces;
    private final ObjectIntMap<String> positions;
    private final IntSet borderPixels;

    public LandProvince(short id, Country countryOwner, Country countryController, Population population, Terrain terrain, List<Country> countriesCore, ResourceGood resourceGood, ObjectIntMap<Building> buildings) {
        this.id = id;
        this.countryOwner = countryOwner;
        this.countryController = countryController;
        this.population = population;
        this.terrain = terrain;
        this.resourceGood = resourceGood;
        this.countriesCore = countriesCore;
        this.buildings = buildings;
        this.adjacentProvinces = new ObjectList<>();
        this.positions = new ObjectIntMap<>();
        this.borderPixels = new IntSet();
    }

    public int getColor() {
        return this.color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public Country getCountryOwner() {
        return this.countryOwner;
    }

    public void setCountryOwner(Country countryOwner) {
        this.countryOwner = countryOwner;
    }

    public Country getCountryController() {
        return this.countryController;
    }

    public void setCountryController(Country countryController) {
        this.countryController = countryController;
    }

    public Terrain getTerrain() {
        return this.terrain;
    }

    public IntSet getBorderPixels() {
        return this.borderPixels;
    }

    public void addBorderPixel(short x, short y) {
        this.borderPixels.add((x << 16) | (y & 0xFFFF));
    }

    public short getId() {
        return this.id;
    }

    public void setId(short id) {
        this.id = id;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public Region getRegion() {
        return this.region;
    }

    public void setContinent(Continent continent) {
        this.continent = continent;
    }

    @Override
    public void addPosition(String name, int position) {
        this.positions.put(name, position);
    }

    @Override
    public int getPosition(String name) {
        return this.positions.get(name);
    }

    @Override
    public void addAdjacentProvinces(Province province) {
        this.adjacentProvinces.add(province);
    }

    public List<Province> getAdjacentProvinces() {
        return this.adjacentProvinces;
    }

    public Continent getContinent() {
        return this.continent;
    }

    public Population getPopulation() {
        return this.population;
    }

    public void setPopulation(Population population) {
        this.population = population;
    }

    public List<Country> getCountriesCore() {
        return this.countriesCore;
    }

    public void addCountryCore(Country country) {
        this.countriesCore.add(country);
    }

    public void removeCountryCore(Country country) {
        this.countriesCore.remove(country);
    }

    public Good getResourceGood() {
        return this.resourceGood;
    }

    public ObjectIntMap<Building> getBuildings() {
        return this.buildings;
    }

    public void addBuilding(Building building) {
        this.buildings.put(building, 1);
    }

    public void removeBuilding(Building building) {
        this.buildings.remove(building);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LandProvince province = (LandProvince) o;
        return this.id == province.id;
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public String toString() {
        return "Province{" +
                "id=" + this.id +
                ", color='" + this.color + '\'' +
                ", number_border_pixels=" + this.borderPixels.size() +
                ", owner=" + this.countryOwner.getName() +
                ", controller=" + this.countryController.getName() +
                ", number_adjacentProvinces=" + this.adjacentProvinces.size() +
                ", region=" + this.region.getId() +
                ", continent=" + this.continent.getName() +
                ", population=" + this.population +
                ", terrain=" + this.terrain.getName() +
                '}';
    }
}

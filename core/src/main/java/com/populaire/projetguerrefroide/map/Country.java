package com.populaire.projetguerrefroide.map;

import com.github.tommyettinger.ds.*;
import com.populaire.projetguerrefroide.entity.Government;
import com.populaire.projetguerrefroide.entity.Ideology;
import com.populaire.projetguerrefroide.entity.Minister;
import com.populaire.projetguerrefroide.national.Attitude;
import com.populaire.projetguerrefroide.national.Culture;
import com.populaire.projetguerrefroide.national.Identity;
import com.populaire.projetguerrefroide.national.Religion;

import java.util.*;
import java.util.List;

public class Country {
    private final String id;
    private final String name;
    private final int color;
    private Set<Region> regions;
    private List<LandProvince> provinces;
    private IntObjectMap<Minister> ministers;
    private LandProvince capital;
    private Government government;
    private Ideology ideology;
    private int headOfGovernment;
    private int headOfState;
    private Culture culture;
    private Identity identity;
    private Religion religion;
    private Attitude attitude;
    private List<MapLabel> labels;

    public Country(String id, String name, int color) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.regions = new ObjectSet<>();
        this.provinces = new ObjectList<>();
        this.ministers = new IntObjectMap<>();
        this.capital = null;
        this.government = null;
        this.ideology = null;
        this.headOfGovernment = -1;
        this.headOfState = -1;
        this.culture = null;
        this.identity = null;
        this.religion = null;
        this.attitude = null;
        this.labels = null;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public int getColor() {
        return this.color;
    }

    public void addRegion(Region region) {
        this.regions.add(region);
    }

    public Set<Region> getRegions() {
        return this.regions;
    }

    public void addProvince(LandProvince province) {
        this.provinces.add(province);
    }

    public List<LandProvince> getProvinces() {
        return this.provinces;
    }

    public void addMinister(int ministerId, Minister minister) {
        this.ministers.put(ministerId, minister);
    }

    public IntObjectMap<Minister> getMinisters() {
        return this.ministers;
    }

    public void setCapital(LandProvince capital) {
        this.capital = capital;
    }

    public LandProvince getCapital() {
        return this.capital;
    }

    public void setGovernment(Government government) {
        this.government = government;
    }

    public Government getGovernment() {
        return this.government;
    }

    public void setIdeology(Ideology ideology) {
        this.ideology = ideology;
    }

    public Ideology getIdeology() {
        return this.ideology;
    }

    public void setHeadOfGovernment(int idMinister) {
        this.headOfGovernment = idMinister;
    }

    public Minister getHeadOfState() {
        return this.ministers.get(this.headOfState);
    }

    public void setHeadOfState(int idMinister) {
        this.headOfState = idMinister;
    }

    public Minister getHeadOfGovernment() {
        return this.ministers.get(this.headOfGovernment);
    }

    public void setCulture(Culture culture) {
        this.culture = culture;
    }

    public Culture getCulture() {
        return this.culture;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    public Identity getIdentity() {
        return this.identity;
    }

    public void setReligion(Religion religion) {
        this.religion = religion;
    }

    public Religion getReligion() {
        return this.religion;
    }

    public void setAttitude(Attitude attitude) {
        this.attitude = attitude;
    }

    public Attitude getAttitude() {
        return this.attitude;
    }

    public int getPopulationSize() {
        int population = 0;
        for(LandProvince province : this.provinces) {
            population += province.getPopulation().getSize();
        }
        return population;
    }

    public List<MapLabel> getLabels() {
        return this.labels;
    }

    public void createLabels() {
        this.labels = new ObjectList<>();
        Set<LandProvince> visitedProvinces = new ObjectSet<>();

        for (LandProvince province : this.provinces) {
            if (!visitedProvinces.contains(province)) {
                List<LandProvince> connectedProvinces = new ObjectList<>();
                this.getConnectedProvinces(province, visitedProvinces, connectedProvinces);
                if(connectedProvinces.size() > 5 || (connectedProvinces.size() == this.provinces.size() && connectedProvinces.size() > 1)) {
                    IntList positionsProvinces = new IntList();
                    IntList pixelsBorderProvinces = new IntList();
                    for(LandProvince connectedProvince : connectedProvinces) {
                        positionsProvinces.add(connectedProvince.getPosition("default"));
                        pixelsBorderProvinces.addAll(connectedProvince.getBorderPixels());
                    }
                    MapLabel label = new MapLabel(this.getName(), pixelsBorderProvinces, positionsProvinces);
                    this.labels.add(label);
                }
            }
        }
    }

    public void getConnectedProvinces(LandProvince province, Set<LandProvince> visitedProvinces, List<LandProvince> connectedProvinces) {
        visitedProvinces.add(province);
        connectedProvinces.add(province);
        for(Province adjacentProvince : province.getAdjacentProvinces()) {
            if(adjacentProvince instanceof LandProvince adjacentLandProvince) {
                if(!visitedProvinces.contains(adjacentProvince) && adjacentLandProvince.getCountryOwner().equals(this)) {
                    this.getConnectedProvinces(adjacentLandProvince, visitedProvinces, connectedProvinces);
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Country country = (Country) o;
        return this.id.equals(country.id);
    }

    @Override
    public String toString() {
        return "Country{" +
                "id='" + this.id + '\'' +
                ", name='" + this.name + '\'' +
                ", color='" + this.color + '\'' +
                '}';
    }
}

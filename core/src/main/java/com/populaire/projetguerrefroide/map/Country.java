package com.populaire.projetguerrefroide.map;

import com.badlogic.gdx.graphics.Color;
import com.populaire.projetguerrefroide.entities.Minister;

import java.util.*;

public class Country {
    private final String id;
    private final String name;
    private final Color color;
    private List<LandProvince> provinces;
    private Map<Integer, Minister> ministers;
    private LandProvince capital;
    private String government;
    private String ideology;
    private Integer headOfGovernment;
    private Integer headOfState;

    public Country(String id, String name, Color color) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.provinces = new ArrayList<>();
        this.ministers = new HashMap<>();
        this.capital = null;
        this.government = "";
        this.ideology = "";
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Color getColor() {
        return this.color;
    }
    public void addProvince(LandProvince province) {
        this.provinces.add(province);
    }

    public List<LandProvince> getProvinces() {
        return this.provinces;
    }

    public void addMinister(Integer ministerId, Minister minister) {
        this.ministers.put(ministerId, minister);
    }

    public Map<Integer, Minister> getMinisters() {
        return this.ministers;
    }

    public void setCapital(LandProvince capital) {
        this.capital = capital;
    }

    public LandProvince getCapital() {
        return this.capital;
    }

    public void setGovernment(String government) {
        this.government = government;
    }

    public String getGovernment() {
        return this.government;
    }

    public void setIdeology(String ideology) {
        this.ideology = ideology;
    }

    public String getIdeology() {
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

    public int getPopulationSize() {
        int population = 0;
        for(LandProvince province : this.provinces) {
            population += province.getPopulation().getSize();
        }
        return population;
    }

    public List<Pixel> getPixelsBorder() {
        List<Pixel> pixelsBorder = new ArrayList<>();
        for(LandProvince province : this.provinces) {
            pixelsBorder.addAll(province.getPixelsBorder());
        }

        return pixelsBorder;
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

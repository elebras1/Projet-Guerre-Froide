package com.populaire.projetguerrefroide.map;

import com.badlogic.gdx.graphics.Color;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.IntSet;
import com.populaire.projetguerrefroide.entities.Minister;
import com.populaire.projetguerrefroide.entities.Population;

import java.util.*;
import java.util.List;

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
    private List<MapLabel> labels;

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

    public IntList getProvincesPixelsBorder() {
        IntList pixelsBorder = new IntList();
        for(LandProvince province : this.provinces) {
            pixelsBorder.addAll(province.getPixelsBorder());
        }

        return pixelsBorder;
    }

    public IntList getPixelsBorder(List<LandProvince> provinces) {
        IntList pixelsBorder = new IntList();
        IntSet provincesPixels = new IntSet();
        for(LandProvince province : provinces) {
            provincesPixels.addAll(province.getPixels());
        }
        for(IntSet.IntSetIterator iterator = provincesPixels.iterator(); iterator.hasNext();) {
            int pixelInt = iterator.nextInt();
            if(this.isPixelBorder((short)(pixelInt >> 16), (short) (pixelInt & 0xFFFF), provincesPixels)) {
                pixelsBorder.add(pixelInt);
            }
        }

        return pixelsBorder;
    }

    public boolean isPixelBorder(short x, short y, IntSet pixels) {
        return !pixels.contains((x + 1 << 16) | (y & 0xFFFF))
            || !pixels.contains((x - 1 << 16) | (y & 0xFFFF))
            || !pixels.contains((x << 16) | (y + 1 & 0xFFFF))
            || !pixels.contains((x << 16) | (y - 1 & 0xFFFF));
    }

    public void createLabels() {
        this.labels = new ArrayList<>();
        Set<LandProvince> visitedProvinces = new HashSet<>();

        for (LandProvince province : this.provinces) {
            if (!visitedProvinces.contains(province)) {
                List<LandProvince> connectedProvinces = new ArrayList<>();
                this.getConnectedProvinces(province, visitedProvinces, connectedProvinces);
                if(connectedProvinces.size() > 5 || (connectedProvinces.size() == this.provinces.size() && connectedProvinces.size() > 1)) {
                    MapLabel label = new MapLabel(this.getName(), this.getPixelsBorder(connectedProvinces));
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

    public List<MapLabel> getLabels() {
        return this.labels;
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

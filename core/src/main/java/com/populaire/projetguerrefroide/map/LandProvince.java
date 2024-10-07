package com.populaire.projetguerrefroide.map;

import com.badlogic.gdx.graphics.Color;
import com.populaire.projetguerrefroide.entities.Population;

import java.util.*;

public class LandProvince implements Province {
    private List<Province> adjacentProvinces;
    private final Set<Pixel> pixels;
    private Color color;
    private short id;
    private String name;
    private Country countryOwner;
    private Country countryController;
    private Region region;
    private Continent continent;
    private Population population;

    public LandProvince(short id, Country countryOwner, Country countryController, Population population) {
        this.id = id;
        this.countryOwner = countryOwner;
        this.countryController = countryController;
        this.population = population;
        this.adjacentProvinces = new ArrayList<>();
        this.pixels = new HashSet<>();
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Country getCountryOwner() {
        return this.countryOwner;
    }

    public Country getCountryController() {
        return this.countryController;
    }

    public Set<Pixel> getPixels() {
        return this.pixels;
    }

    public void addPixel(short x, short y) {
        pixels.add(new Pixel(x, y));
    }

    public short getId() {
        return this.id;
    }

    public void setId(short id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
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

    public void addAllAdjacentProvince(List<Province> provinces) {
        this.adjacentProvinces.addAll(provinces);
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

    public boolean isPixelProvince(short x, short y) {
        return this.pixels.contains(new Pixel(x, y));
    }

    public boolean isPixelBorder(short x, short y) {
        return !this.pixels.contains(new Pixel((short) (x + 1), y))
            || !this.pixels.contains(new Pixel((short) (x - 1), y))
            || !this.pixels.contains(new Pixel(x, (short) (y + 1)))
            || !this.pixels.contains(new Pixel(x, (short) (y - 1)));
    }

    public List<Pixel> getPixelsBorder() {
        List<Pixel> pixelsBorder = new ArrayList<>();
        for(Pixel pixel : this.pixels) {
            if(this.isPixelBorder(pixel.getX(), pixel.getY())) {
                pixelsBorder.add(pixel);
            }
        }
        return pixelsBorder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LandProvince province = (LandProvince) o;
        return this.id == province.id;
    }

    @Override
    public String toString() {
        return "Province{" +
                "id=" + this.id +
                ", color='" + this.color + '\'' +
                ", name='" + this.name + '\'' +
                ", number_pixels=" + this.pixels.size() +
                ", owner=" + this.countryOwner.getName() +
                ", controller=" + this.countryController.getName() +
                ", number_adjacentProvinces=" + this.adjacentProvinces.size() +
                '}';
    }
}

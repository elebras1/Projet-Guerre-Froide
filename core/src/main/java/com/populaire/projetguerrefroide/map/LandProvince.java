package com.populaire.projetguerrefroide.map;

import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.IntSet;
import com.github.tommyettinger.ds.ObjectIntMap;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.economy.population.Population;

import java.util.*;

public class LandProvince implements Province {
    private final List<Province> adjacentProvinces;
    private final ObjectIntMap<String> positions;
    private final IntSet pixels;
    private int color;
    private short id;
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
        this.adjacentProvinces = new ObjectList<>();
        this.positions = new ObjectIntMap<>();
        this.pixels = new IntSet();
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

    public Country getCountryController() {
        return this.countryController;
    }

    public IntSet getPixels() {
        return this.pixels;
    }

    public void addPixel(short x, short y) {
        this.pixels.add((x << 16) | (y & 0xFFFF));
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

    @Override
    public boolean isPixelProvince(short x, short y) {
        return this.pixels.contains((x << 16) | (y & 0xFFFF));
    }

    public boolean isPixelBorder(short x, short y) {
        for (Province adjacentProvince : this.adjacentProvinces) {
            if(adjacentProvince.isPixelProvince((short) (x + 1), y)
                || adjacentProvince.isPixelProvince((short) (x - 1), y)
                || adjacentProvince.isPixelProvince(x, (short) (y + 1))
                || adjacentProvince.isPixelProvince(x, (short) (y - 1))) {
                return true;
            }
        }
        return false;
    }

    public IntList getPixelsBorder() {
        IntList pixelsBorder = new IntList();
        for(IntSet.IntSetIterator iterator = this.pixels.iterator(); iterator.hasNext();) {
            int pixelInt = iterator.nextInt();
            if(this.isPixelBorder((short)(pixelInt >> 16), (short) (pixelInt & 0xFFFF))) {
                pixelsBorder.add(pixelInt);
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
    public int hashCode() {
        return this.id;
    }

    @Override
    public String toString() {
        return "Province{" +
                "id=" + this.id +
                ", color='" + this.color + '\'' +
                ", number_pixels=" + this.pixels.size() +
                ", owner=" + this.countryOwner.getName() +
                ", controller=" + this.countryController.getName() +
                ", number_adjacentProvinces=" + this.adjacentProvinces.size() +
                '}';
    }
}

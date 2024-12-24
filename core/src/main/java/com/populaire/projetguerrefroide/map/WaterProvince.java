package com.populaire.projetguerrefroide.map;

import com.github.tommyettinger.ds.ObjectList;

import java.util.List;

public class WaterProvince implements Province{
    private final List<Province> adjacentProvinces;
    private int color;
    private short id;

    public WaterProvince(short id) {
        this.id = id;
        this.adjacentProvinces = new ObjectList<>();
    }

    @Override
    public int getColor() {
        return this.color;
    }

    @Override
    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public short getId() {
        return this.id;
    }

    @Override
    public void setId(short id) {
        this.id = id;
    }

    @Override
    public void setAdjacentProvinces(List<Province> provinces) {
        this.adjacentProvinces.addAll(provinces);
    }

    @Override
    public List<Province> getAdjacentProvinces() {
        return this.adjacentProvinces;
    }

    @Override
    public boolean isPixelProvince(short x, short y) {
        return false;
    }
}

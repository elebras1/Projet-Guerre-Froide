package com.populaire.projetguerrefroide.map;

import com.github.tommyettinger.ds.ObjectList;

import java.util.List;

public class WaterProvince implements Province{
    private List<Province> adjacentProvinces;
    private int color;
    private short id;
    private String name;

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
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setAdjacentProvinces(List<Province> provinces) {
        this.adjacentProvinces.addAll(provinces);
    }

    @Override
    public List<Province> getAdjacentProvinces() {
        return this.adjacentProvinces;
    }
}

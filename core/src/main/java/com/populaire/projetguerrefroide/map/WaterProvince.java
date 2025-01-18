package com.populaire.projetguerrefroide.map;

import com.github.tommyettinger.ds.ObjectIntMap;
import com.github.tommyettinger.ds.ObjectList;

import java.util.List;

public class WaterProvince implements Province{
    private final List<Province> adjacentProvinces;
    private final ObjectIntMap<String> positions;
    private int color;
    private short id;

    public WaterProvince(short id) {
        this.id = id;
        this.adjacentProvinces = new ObjectList<>();
        this.positions = new ObjectIntMap<>();
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

    @Override
    public List<Province> getAdjacentProvinces() {
        return this.adjacentProvinces;
    }

    @Override
    public boolean isPixelProvince(short x, short y) {
        return false;
    }
}

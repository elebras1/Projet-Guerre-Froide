package com.populaire.projetguerrefroide.map;

import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WaterProvince implements Province{
    private List<Province> adjacentProvinces;
    private final Set<Pixel> pixels;
    private Color color;
    private short id;
    private String name;

    public WaterProvince(short id) {
        this.id = id;
        this.adjacentProvinces = new ArrayList<>();
        this.pixels = new HashSet<>();
    }
    @Override
    public Color getColor() {
        return this.color;
    }

    @Override
    public void setColor(Color color) {
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
    public void addAllAdjacentProvince(List<Province> provinces) {
        this.adjacentProvinces.addAll(provinces);
    }

    @Override
    public List<Province> getAdjacentProvinces() {
        return this.adjacentProvinces;
    }
}

package com.populaire.projetguerrefroide.map;

import com.badlogic.gdx.graphics.Color;

import java.util.HashSet;
import java.util.Set;

public class WaterProvince implements Province{
    private final Set<Pixel> pixels;
    private Color color;
    private short id;
    private String name;

    public WaterProvince(short id) {
        this.id = id;
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
    public Set<Pixel> getPixels() {
        return this.pixels;
    }

    @Override
    public void addPixel(short x, short y) {
        this.pixels.add(new Pixel(x, y));
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
    public boolean isPixelProvince(short x, short y) {
        return this.pixels.contains(new Pixel(x, y));
    }
}

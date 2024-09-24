package com.populaire.projetguerrefroide.map;

import com.badlogic.gdx.graphics.Color;

import java.util.Set;

public interface Province {
    public Color getColor();

    public void setColor(Color color);

    public Set<Pixel> getPixels();

    public void addPixel(short x, short y);

    public short getId();

    public void setId(short id);

    public String getName();

    public void setName(String name);

    public boolean isPixelProvince(short x, short y);
}

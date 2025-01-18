package com.populaire.projetguerrefroide.map;

import com.github.tommyettinger.ds.ObjectIntMap;

import java.util.List;

public interface Province {
    int getColor();
    void setColor(int color);
    short getId();
    void setId(short id);
    void addPosition(String name, int position);
    int getPosition(String name);
    void addAdjacentProvinces(Province province);
    List<Province> getAdjacentProvinces();
    boolean isPixelProvince(short x, short y);

}

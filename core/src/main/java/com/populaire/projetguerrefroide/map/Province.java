package com.populaire.projetguerrefroide.map;

import java.util.List;

public interface Province {
    int getColor();
    void setColor(int color);
    short getId();
    void setId(short id);
    String getName();
    void setName(String name);
    void setAdjacentProvinces(List<Province> provinces);
    List<Province> getAdjacentProvinces();
    boolean isPixelProvince(short x, short y);

}

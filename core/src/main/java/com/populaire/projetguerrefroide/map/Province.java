package com.populaire.projetguerrefroide.map;

import com.badlogic.gdx.graphics.Color;

import java.util.List;

public interface Province {
    Color getColor();
    void setColor(Color color);
    short getId();
    void setId(short id);
    String getName();
    void setName(String name);
    void setAdjacentProvinces(List<Province> provinces);
    List<Province> getAdjacentProvinces();

}

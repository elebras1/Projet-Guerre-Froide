package com.populaire.projetguerrefroide.map;

import java.util.List;

public interface Province {
    short getId();
    void setId(short id);
    void addPosition(String name, int position);
    int getPosition(String name);
    void addAdjacentProvinces(Province province);
    List<Province> getAdjacentProvinces();
}

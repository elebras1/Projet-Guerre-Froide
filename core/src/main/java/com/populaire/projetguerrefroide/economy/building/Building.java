package com.populaire.projetguerrefroide.economy.building;

import com.github.tommyettinger.ds.ObjectFloatMap;
import com.populaire.projetguerrefroide.economy.good.Good;

import java.util.Objects;

public abstract class Building {
    private final String name;
    private final short time;
    private final ObjectFloatMap<Good> goodsCost;

    public Building(String name, short time, ObjectFloatMap<Good> goodsCost) {
        this.name = name;
        this.time = time;
        this.goodsCost = goodsCost;
    }

    public String getName() {
        return this.name;
    }

    public short getTime() {
        return this.time;
    }

    public boolean isOnMap() {
        return false;
    }

    public ObjectFloatMap<Good> getGoodsCost() {
        return this.goodsCost;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Building building)) return false;

        return Objects.equals(this.name, building.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
    }
}

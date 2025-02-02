package com.populaire.projetguerrefroide.economy.building;

import java.util.Objects;

public abstract class Building {
    private final String name;
    private final int cost;
    private final short time;

    public Building(String name, int cost, short time) {
        this.name = name;
        this.cost = cost;
        this.time = time;
    }

    public String getName() {
        return this.name;
    }

    public int getCost() {
        return this.cost;
    }

    public short getTime() {
        return this.time;
    }

    public boolean isOnMap() {
        return false;
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

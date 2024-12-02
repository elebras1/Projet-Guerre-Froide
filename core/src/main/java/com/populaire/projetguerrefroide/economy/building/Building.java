package com.populaire.projetguerrefroide.economy.building;

import java.util.Objects;

public abstract class Building {
    private final String name;
    private final int initialCost;
    private final short time;
    private final boolean onMap;
    private final boolean visibility;

    public Building(String name, int initialCost, short time, boolean onMap, boolean visibility) {
        this.name = name;
        this.initialCost = initialCost;
        this.time = time;
        this.onMap = onMap;
        this.visibility = visibility;
    }

    public String getName() {
        return this.name;
    }

    public int getInitialCost() {
        return this.initialCost;
    }

    public short getTime() {
        return this.time;
    }

    public boolean isOnMap() {
        return this.onMap;
    }

    public boolean isVisible() {
        return this.visibility;
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

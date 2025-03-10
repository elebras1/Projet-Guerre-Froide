package com.populaire.projetguerrefroide.economy.building;

import com.populaire.projetguerrefroide.entity.Modifier;

public class DevelopmentBuilding extends Building {
    private final int cost;
    private final short maxLevel;
    private final Modifier modifier;
    private final boolean onMap;

    public DevelopmentBuilding(String name, int cost, short time, boolean onMap, short maxLevel, Modifier modifier) {
        super(name, time);
        this.cost = cost;
        this.maxLevel = maxLevel;
        this.modifier = modifier;
        this.onMap = onMap;
    }

    public DevelopmentBuilding(String name, int cost, short time, boolean onMap, short maxLevel) {
        super(name, time);
        this.cost = cost;
        this.maxLevel = maxLevel;
        this.modifier = null;
        this.onMap = onMap;
    }

    public int getCost() {
        return this.cost;
    }

    public short getMaxLevel() {
        return this.maxLevel;
    }

    public Modifier getModifier() {
        return this.modifier;
    }

    @Override
    public boolean isOnMap() {
        return this.onMap;
    }

    @Override
    public String toString() {
        return "DevelopmentBuilding{" +
            "name='" + this.getName() + '\'' +
            ", cost=" + this.cost +
            ", time=" + this.getTime() +
            ", onMap=" + this.isOnMap() +
            ", maxLevel=" + this.maxLevel +
            ", modifier=" + this.modifier +
            '}';
    }
}

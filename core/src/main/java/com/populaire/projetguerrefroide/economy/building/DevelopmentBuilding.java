package com.populaire.projetguerrefroide.economy.building;

import com.populaire.projetguerrefroide.entity.Modifier;

public class DevelopmentBuilding extends Building {
    private final short maxLevel;
    private final Modifier modifier;

    public DevelopmentBuilding(String name, int cost, short time, boolean onMap, boolean visibility, short maxLevel, Modifier modifier) {
        super(name, cost, time, onMap, visibility);
        this.maxLevel = maxLevel;
        this.modifier = modifier;
    }

    public DevelopmentBuilding(String name, int cost, short time, boolean onMap, boolean visibility, short maxLevel) {
        super(name, cost, time, onMap, visibility);
        this.maxLevel = maxLevel;
        this.modifier = null;
    }

    public short getMaxLevel() {
        return this.maxLevel;
    }

    public Modifier getModifier() {
        return this.modifier;
    }

    @Override
    public String toString() {
        return "DevelopmentBuilding{" +
            "name='" + this.getName() + '\'' +
            ", cost=" + this.getCost() +
            ", time=" + this.getTime() +
            ", onMap=" + this.isOnMap() +
            ", visibility=" + this.isVisible() +
            ", maxLevel=" + this.maxLevel +
            ", modifier=" + this.modifier +
            '}';
    }
}

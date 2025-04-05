package com.populaire.projetguerrefroide.economy.building;

import com.github.tommyettinger.ds.ObjectFloatMap;
import com.populaire.projetguerrefroide.economy.good.Good;
import com.populaire.projetguerrefroide.entity.Modifier;

import java.util.List;

public class SpecialBuilding extends Building {
    private final int cost;
    private final List<Modifier> modifiers;

    public SpecialBuilding(String name, int cost, short time, ObjectFloatMap<Good> goodsCost, List<Modifier> modifiers) {
        super(name, time, goodsCost);
        this.cost = cost;
        this.modifiers = modifiers;
    }

    public SpecialBuilding(String name, int cost, short time, ObjectFloatMap<Good> goodsCost) {
        super(name, time, goodsCost);
        this.cost = cost;
        this.modifiers = null;
    }

    public List<Modifier> getModifiers() {
        return this.modifiers;
    }

    @Override
    public String toString() {
        return "SpecialBuilding{" +
            "name='" + this.getName() + '\'' +
            ", cost=" + this.cost +
            ", time=" + this.getTime() +
            ", onMap=" + this.isOnMap() +
            ", modifiers=" + this.modifiers +
            '}';
    }
}

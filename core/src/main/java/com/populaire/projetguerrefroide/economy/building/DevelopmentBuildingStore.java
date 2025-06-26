package com.populaire.projetguerrefroide.economy.building;

import com.github.tommyettinger.ds.*;
import com.populaire.projetguerrefroide.entity.Modifier;

import java.util.List;

public class DevelopmentBuildingStore extends BuildingStore {
    private final IntList costs;
    private final ShortList maxLevels;
    private final List<Modifier> modifiers;
    private final BooleanList onMap;

    public DevelopmentBuildingStore(List<String> names, IntList times, IntList types, IntList onMapFlags, IntList goodsCostGoodIds, FloatList goodsCostValues, IntList goodsCostStart, IntList goodsCostCount, IntList costs, BooleanList onMap, ShortList maxLevels, List<Modifier> modifiers) {
        super(names, times, types, onMapFlags, goodsCostGoodIds, goodsCostValues, goodsCostStart, goodsCostCount);
        this.costs = costs;
        this.maxLevels = maxLevels;
        this.modifiers = modifiers;
        this.onMap = onMap;
    }

    public IntList getCosts() {
        return this.costs;
    }

    public ShortList getMaxLevels() {
        return this.maxLevels;
    }

    public List<Modifier> getModifiers() {
        return this.modifiers;
    }

    public BooleanList getOnMap() {
        return this.onMap;
    }

    @Override
    public String toString() {
        return "DevelopmentBuilding{" +
                "names=" + getNames() +
                ", times=" + getTimes() +
                ", types=" + getTypes() +
                ", onMapFlags=" + getOnMapFlags() +
                ", goodsCostGoodIds=" + getGoodsCostGoodIds() +
                ", goodsCostValues=" + getGoodsCostValues() +
                ", goodsCostStart=" + getGoodsCostStart() +
                ", goodsCostCount=" + getGoodsCostCount() +
                ", costs=" + this.costs +
                ", maxLevels=" + this.maxLevels +
                ", modifiers=" + this.modifiers +
                ", onMap=" + this.onMap +
                '}';
    }
}

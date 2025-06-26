package com.populaire.projetguerrefroide.economy.building;

import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.IntList;
import com.populaire.projetguerrefroide.entity.Modifier;

import java.util.List;

public class SpecialBuildingStore extends BuildingStore {
    private final IntList costs;
    private final List<List<Modifier>> modifiersList;

    public SpecialBuildingStore(List<String> names, IntList times, IntList types, IntList onMapFlags, IntList goodsCostGoodIds, FloatList goodsCostValues, IntList goodsCostStart, IntList goodsCostCount, IntList costs, List<List<Modifier>> modifiersList) {
        super(names, times, types, onMapFlags, goodsCostGoodIds, goodsCostValues, goodsCostStart, goodsCostCount);
        this.costs = costs;
        this.modifiersList = modifiersList;
    }

    public IntList getCosts() {
        return this.costs;
    }

    public List<List<Modifier>> getModifiersList() {
        return this.modifiersList;
    }

    @Override
    public String toString() {
        return "SpecialBuildingStore{" +
            "names=" + getNames() +
            ", times=" + getTimes() +
            ", types=" + getTypes() +
            ", onMapFlags=" + getOnMapFlags() +
            ", costs=" + this.costs +
            ", modifiersList=" + this.modifiersList +
            ", goodsCostGoodIds=" + getGoodsCostGoodIds() +
            ", goodsCostValues=" + getGoodsCostValues() +
            ", goodsCostStart=" + getGoodsCostStart() +
            ", goodsCostCount=" + getGoodsCostCount() +
            '}';
    }
}

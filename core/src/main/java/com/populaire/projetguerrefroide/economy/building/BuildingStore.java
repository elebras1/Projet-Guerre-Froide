package com.populaire.projetguerrefroide.economy.building;

import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.IntList;
import java.util.List;

public class BuildingStore {
    private final List<String> names;
    private final IntList times;
    private final IntList types;
    private final IntList onMapFlags;
    private final IntList goodsCostGoodIds;
    private final FloatList goodsCostValues;
    private final IntList goodsCostStart;
    private final IntList goodsCostCount;

    public BuildingStore(List<String> names, IntList times, IntList types, IntList onMapFlags, IntList goodsCostGoodIds, FloatList goodsCostValues, IntList goodsCostStart, IntList goodsCostCount) {
        this.names = names;
        this.times = times;
        this.types = types;
        this.onMapFlags = onMapFlags;
        this.goodsCostGoodIds = goodsCostGoodIds;
        this.goodsCostValues = goodsCostValues;
        this.goodsCostStart = goodsCostStart;
        this.goodsCostCount = goodsCostCount;
    }

    public List<String> getNames() {
        return this.names;
    }

    public IntList getTimes() {
        return this.times;
    }

    public IntList getTypes() {
        return this.types;
    }

    public IntList getOnMapFlags() {
        return this.onMapFlags;
    }

    public IntList getGoodsCostGoodIds() {
        return this.goodsCostGoodIds;
    }

    public FloatList getGoodsCostValues() {
        return this.goodsCostValues;
    }

    public IntList getGoodsCostStart() {
        return this.goodsCostStart;
    }

    public IntList getGoodsCostCount() {
        return this.goodsCostCount;
    }

    @Override
    public String toString() {
        return "BuildingStore{" +
            "names=" + this.names +
            ", times=" + this.times +
            ", types=" + this.types +
            ", onMapFlags=" + this.onMapFlags +
            ", goodsCostGoodIds=" + this.goodsCostGoodIds +
            ", goodsCostValues=" + this.goodsCostValues +
            ", goodsCostStart=" + this.goodsCostStart +
            ", goodsCostCount=" + this.goodsCostCount +
            '}';
    }
}

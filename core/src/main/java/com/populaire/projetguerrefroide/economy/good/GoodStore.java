package com.populaire.projetguerrefroide.economy.good;

import com.github.tommyettinger.ds.ByteList;
import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.IntList;

import java.util.List;

public class GoodStore {
    private final List<String> names;
    private final FloatList costs;
    private final IntList colors;
    private final ByteList types;

    // Resource goods specific fields
    private final IntList productionTypeIds;
    private final FloatList values;

    public GoodStore(List<String> names, FloatList costs, IntList colors, ByteList types, IntList productionTypeIds, FloatList values) {
        this.names = names;
        this.costs = costs;
        this.colors = colors;
        this.types = types;
        this.productionTypeIds = productionTypeIds;
        this.values = values;
    }

    public FloatList getCosts() {
        return this.costs;
    }

    public List<String> getNames() {
        return this.names;
    }

    public IntList getColors() {
        return this.colors;
    }

    public ByteList getTypes() {
        return this.types;
    }

    public IntList getProductionTypeIds() {
        return this.productionTypeIds;
    }

    public FloatList getValues() {
        return this.values;
    }

    @Override
    public String toString() {
        return "GoodStore{" +
                "names=" + this.names +
                ", costs=" + this.costs +
                ", colors=" + this.colors +
                ", types=" + this.types +
                ", productionTypeIds=" + this.productionTypeIds +
                ", values=" + this.values +
                '}';
    }
}

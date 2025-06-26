package com.populaire.projetguerrefroide.economy.good;

import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.IntList;

import java.util.List;

public class ResourceGoodStore extends GoodStore {
    private final IntList productionTypeIds;
    private final FloatList values;

    public ResourceGoodStore(List<String> names, FloatList costs, IntList colors, IntList productionTypeIds, FloatList values) {
        super(names, costs, colors);
        this.productionTypeIds = productionTypeIds;
        this.values = values;
    }

    public IntList getProductionTypeIds() {
        return this.productionTypeIds;
    }

    public FloatList getValues() {
        return this.values;
    }

    @Override
    public String toString() {
        return "ResourceGood{" +
            "names=" + this.getNames() +
            ", costs=" + this.getCosts() +
            ", colors=" + this.getColors() +
            ", productionTypeIds=" + this.productionTypeIds +
            ", values=" + this.values +
            '}';
    }
}

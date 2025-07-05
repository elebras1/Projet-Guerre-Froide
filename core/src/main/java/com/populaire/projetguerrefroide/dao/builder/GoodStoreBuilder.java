package com.populaire.projetguerrefroide.dao.builder;

import com.github.tommyettinger.ds.ByteList;
import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.economy.good.GoodStore;

import java.util.List;

public class GoodStoreBuilder {
    private final int defaultCapacity;
    private int index;
    private final List<String> names;
    private final FloatList costs;
    private final IntList colors;
    private final ByteList types;
    private final IntList productionTypeIds;
    private final FloatList values;

    public GoodStoreBuilder() {
        this.defaultCapacity = 40;
        this.index = 0;
        this.names = new ObjectList<>(this.defaultCapacity);
        this.costs = new FloatList(this.defaultCapacity);
        this.colors = new IntList(this.defaultCapacity);
        this.types = new ByteList(this.defaultCapacity);
        this.productionTypeIds = new IntList(this.defaultCapacity);
        this.values = new FloatList(this.defaultCapacity);
    }

    public int getDefaultCapacity() {
        return this.defaultCapacity;
    }

    public int getIndex() {
        return this.index;
    }

    public void addGood(String name, float cost, int color, byte type, int productionTypeId, float value) {
        this.names.add(name);
        this.costs.add(cost);
        this.colors.add(color);
        this.types.add(type);
        this.productionTypeIds.add(productionTypeId);
        this.values.add(value);

        this.index = this.names.size() - 1;
    }

    public GoodStore build() {
        return new GoodStore(this.names, this.costs, this.colors, this.types, this.productionTypeIds, this.values);
    }
}

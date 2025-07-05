package com.populaire.projetguerrefroide.dao.builder;

import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.economy.population.PopulationTypeStore;

import java.util.List;

public class PopulationTypeStoreBuilder {
    private final int defaultCapacity;
    private int index;
    private final IntList colors;
    private final List<String> names;
    private final IntList standardDemandGoodIds;
    private final FloatList standardDemandValues;
    private final IntList standardDemandStarts;
    private final IntList standardDemandCounts;
    private final IntList luxuryDemandGoodIds;
    private final FloatList luxuryDemandValues;
    private final IntList luxuryDemandStarts;
    private final IntList luxuryDemandCounts;

    public PopulationTypeStoreBuilder() {
        this.defaultCapacity = 12;
        this.index = 0;
        this.colors = new IntList(this.defaultCapacity);
        this.names = new ObjectList<>(this.defaultCapacity);
        this.standardDemandGoodIds = new IntList();
        this.standardDemandValues = new FloatList();
        this.standardDemandStarts = new IntList(this.defaultCapacity);
        this.standardDemandCounts = new IntList(this.defaultCapacity);
        this.luxuryDemandGoodIds = new IntList();
        this.luxuryDemandValues = new FloatList();
        this.luxuryDemandStarts = new IntList(this.defaultCapacity);
        this.luxuryDemandCounts = new IntList(this.defaultCapacity);
    }

    public int getDefaultCapacity() {
        return this.defaultCapacity;
    }

    public int getIndex() {
        return this.index;
    }

    public PopulationTypeStoreBuilder addPopulationType(String name, int color) {
        this.names.add(name);
        this.colors.add(color);

        this.standardDemandStarts.add(this.standardDemandGoodIds.size());
        this.standardDemandCounts.add(0);
        this.luxuryDemandStarts.add(this.luxuryDemandGoodIds.size());
        this.luxuryDemandCounts.add(0);

        this.index = this.names.size() - 1;
        return this;
    }

    public PopulationTypeStoreBuilder addStandardDemand(int goodId, float value) {
        this.standardDemandGoodIds.add(goodId);
        this.standardDemandValues.add(value);

        int currentCount = this.standardDemandCounts.get(this.index);
        this.standardDemandCounts.set(this.index, currentCount + 1);

        return this;
    }

    public PopulationTypeStoreBuilder addLuxuryDemand(int goodId, float value) {
        this.luxuryDemandGoodIds.add(goodId);
        this.luxuryDemandValues.add(value);

        int currentCount = this.luxuryDemandCounts.get(this.index);
        this.luxuryDemandCounts.set(this.index, currentCount + 1);

        return this;
    }

    public PopulationTypeStore build() {
        return new PopulationTypeStore(this.colors, this.names, this.standardDemandGoodIds, this.standardDemandValues, this.standardDemandStarts, this.standardDemandCounts, this.luxuryDemandGoodIds, this.luxuryDemandValues, this.luxuryDemandStarts, this.luxuryDemandCounts);
    }
}

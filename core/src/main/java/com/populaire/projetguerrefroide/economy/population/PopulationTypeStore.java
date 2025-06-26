package com.populaire.projetguerrefroide.economy.population;

import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.IntList;

import java.util.List;

public class PopulationTypeStore {
    private final IntList colors;
    private final List<String> names;
    private final IntList standardDemandGoodIds;
    private final FloatList standardDemandValues;
    private final IntList standardDemandStart;
    private final IntList standardDemandCount;
    private final IntList luxuryDemandGoodIds;
    private final FloatList luxuryDemandValues;
    private final IntList luxuryDemandStart;
    private final IntList luxuryDemandCount;

    public PopulationTypeStore(IntList colors, List<String> names, IntList standardDemandGoodIds, FloatList standardDemandValues, IntList standardDemandStart, IntList standardDemandCount, IntList luxuryDemandGoodIds, FloatList luxuryDemandValues, IntList luxuryDemandStart, IntList luxuryDemandCount) {
        this.colors = colors;
        this.names = names;
        this.standardDemandGoodIds = standardDemandGoodIds;
        this.standardDemandValues = standardDemandValues;
        this.standardDemandStart = standardDemandStart;
        this.standardDemandCount = standardDemandCount;
        this.luxuryDemandGoodIds = luxuryDemandGoodIds;
        this.luxuryDemandValues = luxuryDemandValues;
        this.luxuryDemandStart = luxuryDemandStart;
        this.luxuryDemandCount = luxuryDemandCount;
    }

    public IntList getColors() {
        return this.colors;
    }

    public List<String> getNames() {
        return this.names;
    }

    public IntList getStandardDemandGoodIds() {
        return this.standardDemandGoodIds;
    }

    public FloatList getStandardDemandValues() {
        return this.standardDemandValues;
    }

    public IntList getStandardDemandStart() {
        return this.standardDemandStart;
    }

    public IntList getStandardDemandCount() {
        return this.standardDemandCount;
    }

    public IntList getLuxuryDemandGoodIds() {
        return this.luxuryDemandGoodIds;
    }

    public FloatList getLuxuryDemandValues() {
        return this.luxuryDemandValues;
    }

    public IntList getLuxuryDemandStart() {
        return this.luxuryDemandStart;
    }

    public IntList getLuxuryDemandCount() {
        return this.luxuryDemandCount;
    }

    @Override
    public String toString() {
        return "PopulationTypeStore{" +
                "colors=" + this.colors +
                ", names=" + this.names +
                ", standardDemandGoodIds=" + this.standardDemandGoodIds +
                ", standardDemandValues=" + this.standardDemandValues +
                ", standardDemandStart=" + this.standardDemandStart +
                ", standardDemandCount=" + this.standardDemandCount +
                ", luxuryDemandGoodIds=" + this.luxuryDemandGoodIds +
                ", luxuryDemandValues=" + this.luxuryDemandValues +
                ", luxuryDemandStart=" + this.luxuryDemandStart +
                ", luxuryDemandCount=" + this.luxuryDemandCount +
                '}';
    }
}

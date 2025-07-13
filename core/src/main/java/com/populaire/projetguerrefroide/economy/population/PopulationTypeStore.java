package com.populaire.projetguerrefroide.economy.population;

import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.IntList;

import java.util.List;

public class PopulationTypeStore {
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

    public PopulationTypeStore(IntList colors, List<String> names, IntList standardDemandGoodIds, FloatList standardDemandValues, IntList standardDemandStarts, IntList standardDemandCounts, IntList luxuryDemandGoodIds, FloatList luxuryDemandValues, IntList luxuryDemandStarts, IntList luxuryDemandCounts) {
        this.colors = colors;
        this.names = names;
        this.standardDemandGoodIds = standardDemandGoodIds;
        this.standardDemandValues = standardDemandValues;
        this.standardDemandStarts = standardDemandStarts;
        this.standardDemandCounts = standardDemandCounts;
        this.luxuryDemandGoodIds = luxuryDemandGoodIds;
        this.luxuryDemandValues = luxuryDemandValues;
        this.luxuryDemandStarts = luxuryDemandStarts;
        this.luxuryDemandCounts = luxuryDemandCounts;
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

    public IntList getStandardDemandStarts() {
        return this.standardDemandStarts;
    }

    public IntList getStandardDemandCounts() {
        return this.standardDemandCounts;
    }

    public IntList getLuxuryDemandGoodIds() {
        return this.luxuryDemandGoodIds;
    }

    public FloatList getLuxuryDemandValues() {
        return this.luxuryDemandValues;
    }

    public IntList getLuxuryDemandStarts() {
        return this.luxuryDemandStarts;
    }

    public IntList getLuxuryDemandCounts() {
        return this.luxuryDemandCounts;
    }

    @Override
    public String toString() {
        return "PopulationTypeStore{" +
                "colors=" + this.colors +
                ", names=" + this.names +
                ", standardDemandGoodIds=" + this.standardDemandGoodIds +
                ", standardDemandValues=" + this.standardDemandValues +
                ", standardDemandStart=" + this.standardDemandStarts +
                ", standardDemandCount=" + this.standardDemandCounts +
                ", luxuryDemandGoodIds=" + this.luxuryDemandGoodIds +
                ", luxuryDemandValues=" + this.luxuryDemandValues +
                ", luxuryDemandStarts=" + this.luxuryDemandStarts +
                ", luxuryDemandCounts=" + this.luxuryDemandCounts +
                '}';
    }
}

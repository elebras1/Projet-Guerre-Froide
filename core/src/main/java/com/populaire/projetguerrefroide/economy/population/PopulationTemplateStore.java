package com.populaire.projetguerrefroide.economy.population;

import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.IntIntMap;

public class PopulationTemplateStore {
    private final IntIntMap indexById;
    private final FloatList children;
    private final FloatList adults;
    private final FloatList seniors;

    public PopulationTemplateStore(IntIntMap indexById, FloatList children, FloatList adults, FloatList seniors) {
        this.indexById = indexById;
        this.children = children;
        this.adults = adults;
        this.seniors = seniors;
    }

    public IntIntMap getIndexById() {
        return this.indexById;
    }

    public FloatList getChildren() {
        return this.children;
    }

    public FloatList getAdults() {
        return this.adults;
    }

    public FloatList getSeniors() {
        return this.seniors;
    }

    @Override
    public String toString() {
        return "PopulationTemplateStore{" +
                "indexById=" + this.indexById +
                ", children=" + this.children +
                ", adults=" + this.adults +
                ", seniors=" + this.seniors +
                '}';
    }
}

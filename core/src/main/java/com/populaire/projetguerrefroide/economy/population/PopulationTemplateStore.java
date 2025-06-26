package com.populaire.projetguerrefroide.economy.population;

import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.ShortList;

public class PopulationTemplateStore {
    private final ShortList ids;
    private final FloatList children;
    private final FloatList adults;
    private final FloatList seniors;

    public PopulationTemplateStore(ShortList ids, FloatList children, FloatList adults, FloatList seniors) {
        if (ids.size() != children.size() || ids.size() != adults.size() || ids.size() != seniors.size()) {
            throw new IllegalArgumentException("All lists must have the same size");
        }
        this.ids = ids;
        this.children = children;
        this.adults = adults;
        this.seniors = seniors;
    }

    public ShortList getIds() {
        return this.ids;
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
                "ids=" + this.ids +
                ", children=" + this.children +
                ", adults=" + this.adults +
                ", seniors=" + this.seniors +
                '}';
    }
}

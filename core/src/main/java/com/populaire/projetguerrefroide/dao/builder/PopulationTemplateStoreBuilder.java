package com.populaire.projetguerrefroide.dao.builder;

import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.ShortList;
import com.populaire.projetguerrefroide.economy.population.PopulationTemplateStore;

public class PopulationTemplateStoreBuilder {
    private final int defaultCapacity;
    private int index;
    private final ShortList ids;
    private final FloatList children;
    private final FloatList adults;
    private final FloatList seniors;

    public PopulationTemplateStoreBuilder() {
        this.defaultCapacity = 396;
        this.index = 0;
        this.ids = new ShortList(this.defaultCapacity);
        this.children = new FloatList(this.defaultCapacity);
        this.adults = new FloatList(this.defaultCapacity);
        this.seniors = new FloatList(this.defaultCapacity);
    }

    public int getDefaultCapacity() {
        return this.defaultCapacity;
    }

    public int getIndex() {
        return this.index;
    }

    public void add(short id, float children, float adults, float seniors) {
        this.ids.add(id);
        this.children.add(children);
        this.adults.add(adults);
        this.seniors.add(seniors);
        this.index++;
    }

    public PopulationTemplateStore build() {
        return new PopulationTemplateStore(this.ids, this.children, this.adults, this.seniors);
    }
}

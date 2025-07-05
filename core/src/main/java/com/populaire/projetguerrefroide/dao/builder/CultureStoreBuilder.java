package com.populaire.projetguerrefroide.dao.builder;

import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.national.CultureStore;

import java.util.List;

public class CultureStoreBuilder {
    private final int defaultCapacity;
    private int index;
    private final List<String> names;
    private final IntList colors;

    public CultureStoreBuilder() {
        this.defaultCapacity = 409;
        this.names = new ObjectList<>(this.defaultCapacity);
        this.colors = new IntList(this.defaultCapacity);
        this.index = 0;
    }

    public int getDefaultCapacity() {
        return this.defaultCapacity;
    }

    public int getIndex() {
        return this.index;
    }

    public void addCulture(String name, int color) {
        this.names.add(name);
        this.colors.add(color);
        this.index = this.names.size() - 1;
    }

    public CultureStore build() {
        return new CultureStore(this.names, this.colors);
    }
}

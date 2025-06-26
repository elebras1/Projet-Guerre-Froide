package com.populaire.projetguerrefroide.economy.good;

import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.IntList;

import java.util.List;

public abstract class GoodStore {
    private final List<String> names;
    private final FloatList costs;
    private final IntList colors;

    public GoodStore(List<String> names, FloatList costs, IntList colors) {
        this.names = names;
        this.costs = costs;
        this.colors = colors;
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
}

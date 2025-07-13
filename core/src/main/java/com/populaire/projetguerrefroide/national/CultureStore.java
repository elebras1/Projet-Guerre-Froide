package com.populaire.projetguerrefroide.national;

import com.github.tommyettinger.ds.IntList;

import java.util.List;

public class CultureStore {
    private final List<String> names;
    private final IntList colors;

    public CultureStore(List<String> names, IntList colors) {
        this.names = names;
        this.colors = colors;
    }

    public List<String> getNames() {
        return this.names;
    }

    public IntList getColors() {
        return this.colors;
    }

    @Override
    public String toString() {
        return "Culture{" +
                "names='" + this.names + '\'' +
                ", colors=" + this.colors +
                '}';
    }
}

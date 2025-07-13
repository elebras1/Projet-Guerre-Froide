package com.populaire.projetguerrefroide.entity;

import com.github.tommyettinger.ds.CharList;
import com.github.tommyettinger.ds.FloatList;

import java.util.List;

public class ModifierStore {
    private final List<String> names;
    private final FloatList values;
    private final CharList operators;

    public ModifierStore(List<String> names, FloatList values, CharList operators) {
        this.names = names;
        this.values = values;
        this.operators = operators;
    }

    public List<String> getNames() {
        return this.names;
    }

    public FloatList getValues() {
        return this.values;
    }

    public CharList getOperators() {
        return this.operators;
    }

    @Override
    public String toString() {
        return "ModifierStore{" +
            "names=" + this.names +
            ", values=" + this.values +
            ", types=" + this.operators +
            '}';
    }
}

package com.populaire.projetguerrefroide.dao.builder;

import com.github.tommyettinger.ds.CharList;
import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.entity.ModifierStore;

import java.util.List;

public class ModifierStoreBuilder {
    private final int defaultCapacity;
    private int index;
    private final List<String> names;
    private final FloatList values;
    private final CharList operators;

    public ModifierStoreBuilder() {
        this.defaultCapacity = 100;
        this.index = 0;
        this.names = new ObjectList<>(this.defaultCapacity);
        this.values = new FloatList(this.defaultCapacity);
        this.operators = new CharList(this.defaultCapacity);
    }

    public int getDefaultCapacity() {
        return this.defaultCapacity;
    }

    public int getIndex() {
        return this.index;
    }

    public void addModifier(String name, float value, char operator) {
        this.names.add(name);
        this.values.add(value);
        this.operators.add(operator);
        this.index = this.names.size() - 1;
    }

    public void addModifier(String name, float value) {
        this.addModifier(name, value, '+');
    }

    public ModifierStore build() {
        return new ModifierStore(this.names, this.values, this.operators);
    }
}

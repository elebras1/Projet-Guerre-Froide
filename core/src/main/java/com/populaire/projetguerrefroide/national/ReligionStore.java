package com.populaire.projetguerrefroide.national;

import com.github.tommyettinger.ds.IntList;
import com.populaire.projetguerrefroide.entity.Modifier;

import java.util.List;

public class ReligionStore {
    private final List<String> names;
    private final IntList colors;
    private final List<List<Modifier>> modifiers;

    public ReligionStore(List<String> names, IntList colors, List<List<Modifier>> modifiers) {
        this.names = names;
        this.colors = colors;
        this.modifiers = modifiers;
    }

    public List<String> getNames() {
        return this.names;
    }

    public IntList getColor() {
        return this.colors;
    }

    public List<List<Modifier>> getModifiers() {
        return this.modifiers;
    }

    @Override
    public String toString() {
        return "Religion{" +
                "names='" + this.names + '\'' +
                ", colors=" + this.colors +
                ", modifiers=" + this.modifiers +
                '}';
    }
}

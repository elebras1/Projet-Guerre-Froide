package com.populaire.projetguerrefroide.national;

import com.github.tommyettinger.ds.IntList;
import com.populaire.projetguerrefroide.entity.Modifier;

import java.util.List;

public class ReligionStore {
    private final List<String> names;
    private final IntList colors;
    private final IntList modifierIds;
    private final IntList modifierStart;
    private final IntList modifierCount;

    public ReligionStore(List<String> names, IntList colors, IntList modifierIds, IntList modifierStart, IntList modifierCount) {
        this.names = names;
        this.colors = colors;
        this.modifierIds = modifierIds;
        this.modifierStart = modifierStart;
        this.modifierCount = modifierCount;
    }

    public List<String> getNames() {
        return this.names;
    }

    public IntList getColors() {
        return this.colors;
    }

    public IntList getModifierIds() {
        return this.modifierIds;
    }

    public IntList getModifierStart() {
        return this.modifierStart;
    }

    public IntList getModifierCount() {
        return this.modifierCount;
    }

    @Override
    public String toString() {
        return "ReligionStore{" +
                "names=" + this.names +
                ", colors=" + this.colors +
                ", modifierIds=" + this.modifierIds +
                ", modifierStart=" + this.modifierStart +
                ", modifierCount=" + this.modifierCount +
                '}';
    }
}

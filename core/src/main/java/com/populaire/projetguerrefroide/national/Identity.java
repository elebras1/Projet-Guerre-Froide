package com.populaire.projetguerrefroide.national;

import com.github.tommyettinger.ds.IntList;
import com.populaire.projetguerrefroide.util.Named;

import java.util.Objects;

public class Identity implements Named {
    private final String name;
    private final IntList modifierIds;

    public Identity(String name, IntList modifierIds) {
        this.name = name;
        this.modifierIds = modifierIds;
    }

    public String getName() {
        return this.name;
    }

    public IntList getModifierIds() {
        return this.modifierIds;
    }

    @Override
    public String toString() {
        return "Identity{" +
                "name='" + this.name + '\'' +
                ", modifiers=" + this.modifierIds +
                '}';
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Identity identity)) return false;

        return Objects.equals(this.name, identity.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
    }
}

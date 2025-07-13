package com.populaire.projetguerrefroide.national;

import com.github.tommyettinger.ds.IntList;
import com.populaire.projetguerrefroide.util.Named;

import java.util.Objects;

public class Attitude implements Named {
    private final String name;
    private final IntList modifierIds;

    public Attitude(String name, IntList modifierIds) {
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
        return "Attitude{" +
                "name='" + this.name + '\'' +
                ", modifiers=" + this.modifierIds +
                '}';
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attitude attitude)) return false;

        return Objects.equals(this.name, attitude.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
    }
}

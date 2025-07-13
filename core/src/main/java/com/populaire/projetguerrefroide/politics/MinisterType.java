package com.populaire.projetguerrefroide.politics;

import com.github.tommyettinger.ds.IntList;

import java.util.Objects;

public class MinisterType {
    private final String name;
    private final IntList modifierIds;

    public MinisterType(String name, IntList modifierIds) {
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
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MinisterType ministerType)) return false;

        return Objects.equals(this.name, ministerType.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
    }

    @Override
    public String toString() {
        return "MinisterType{" +
            "name='" + this.name + '\'' +
            ", modifiers=" + this.modifierIds +
            '}';
    }
}

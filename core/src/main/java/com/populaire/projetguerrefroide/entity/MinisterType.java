package com.populaire.projetguerrefroide.entity;

import java.util.List;
import java.util.Objects;

public class MinisterType {
    private final String name;
    private final List<Modifier> modifiers;

    public MinisterType(String name, List<Modifier> modifiers) {
        this.name = name;
        this.modifiers = modifiers;
    }

    public String getName() {
        return this.name;
    }

    public List<Modifier> getModifiers() {
        return this.modifiers;
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
            ", modifiers=" + this.modifiers +
            '}';
    }
}

package com.populaire.projetguerrefroide.national;

import com.populaire.projetguerrefroide.entity.Modifier;

import java.util.List;
import java.util.Objects;

public class Identity {
    private final String name;
    private final List<Modifier> modifiers;

    public Identity(String name, List<Modifier> modifiers) {
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
    public String toString() {
        return "Identity{" +
                "name='" + this.name + '\'' +
                ", modifiers=" + this.modifiers +
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

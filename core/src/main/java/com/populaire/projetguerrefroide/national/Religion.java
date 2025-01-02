package com.populaire.projetguerrefroide.national;

import com.populaire.projetguerrefroide.entity.Modifier;

import java.util.List;
import java.util.Objects;

public class Religion {
    private final String name;
    private final int color;
    private final List<Modifier> modifiers;

    public Religion(String name, int color, List<Modifier> modifiers) {
        this.name = name;
        this.color = color;
        this.modifiers = modifiers;
    }

    public String getName() {
        return this.name;
    }

    public int getColor() {
        return this.color;
    }

    public List<Modifier> getModifiers() {
        return this.modifiers;
    }

    @Override
    public String toString() {
        return "Religion{" +
                "name='" + this.name + '\'' +
                ", color=" + this.color +
                ", modifiers=" + this.modifiers +
                '}';
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Religion religion)) return false;

        return Objects.equals(this.name, religion.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
    }
}

package com.populaire.projetguerrefroide.entity;

import java.util.Objects;

public class Modifier {
    private final String name;
    private final float value;
    private final String type;

    public Modifier(String name, float value, String type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public Modifier(String name, float value) {
        this(name, value, "add");
    }

    public String getName() {
        return this.name;
    }

    public float getValue() {
        return this.value;
    }

    public String getType() {
        return this.type;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Modifier modifier)) return false;

        return Objects.equals(this.name, modifier.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
    }

    @Override
    public String toString() {
        return "Modifier{" +
            "name='" + this.name + '\'' +
            ", value=" + this.value +
            ", type='" + this.type + '\'' +
            '}';
    }
}

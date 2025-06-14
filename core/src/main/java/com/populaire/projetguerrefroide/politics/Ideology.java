package com.populaire.projetguerrefroide.politics;

import java.util.Objects;

public class Ideology {
    private final String name;
    private final int color;
    private final short factionDriftingSpeed;

    public Ideology(String name, int color, short factionDriftingSpeed) {
        this.name = name;
        this.color = color;
        this.factionDriftingSpeed = factionDriftingSpeed;
    }

    public String getName() {
        return this.name;
    }

    public int getColor() {
        return this.color;
    }

    public short getFactionDriftingSpeed() {
        return this.factionDriftingSpeed;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ideology ideology)) return false;

        return Objects.equals(this.name, ideology.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
    }

    @Override
    public String toString() {
        return "Ideology{" +
            "name='" + this.name + '\'' +
            ", color=" + this.color +
            ", factionDriftingSpeed=" + this.factionDriftingSpeed +
            '}';
    }
}

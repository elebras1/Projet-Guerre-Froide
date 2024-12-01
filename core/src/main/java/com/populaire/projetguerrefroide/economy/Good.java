package com.populaire.projetguerrefroide.economy;

import java.util.Objects;

public abstract class Good {
    private final String name;
    private final float cost;
    private final int color;

    public Good(String name, float cost, int color) {
        this.name = name;
        this.cost = cost;
        this.color = color;
    }

    public String getName() {
        return this.name;
    }

    public float getCost() {
        return this.cost;
    }

    public int getColor() {
        return this.color;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Good good)) return false;

        return Objects.equals(name, good.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}

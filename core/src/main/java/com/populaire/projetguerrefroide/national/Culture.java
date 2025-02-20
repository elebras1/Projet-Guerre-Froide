package com.populaire.projetguerrefroide.national;

import com.populaire.projetguerrefroide.util.Named;

import java.util.Objects;

public class Culture implements Named {
    private final String name;
    private final int color;

    public Culture(String name, int color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return this.name;
    }

    public int getColor() {
        return this.color;
    }

    @Override
    public String toString() {
        return "Culture{" +
                "name='" + this.name + '\'' +
                ", color=" + this.color +
                '}';
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Culture culture)) return false;

        return Objects.equals(this.name, culture.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
    }
}

package com.populaire.projetguerrefroide.economy.population;

import com.github.tommyettinger.ds.ObjectFloatMap;
import com.populaire.projetguerrefroide.economy.good.Good;

public class PopulationType {
    private final int color;
    private final String name;
    private final ObjectFloatMap<Good> standardDemands;
    private final ObjectFloatMap<Good> luxuryDemands;

    public PopulationType(int color, String name, ObjectFloatMap<Good> standardDemands, ObjectFloatMap<Good> luxuryDemands) {
        this.color = color;
        this.name = name;
        this.standardDemands = standardDemands;
        this.luxuryDemands = luxuryDemands;
    }

    public int getColor() {
        return this.color;
    }

    public String getName() {
        return this.name;
    }

    public ObjectFloatMap<Good> getStandardDemands() {
        return this.standardDemands;
    }

    public ObjectFloatMap<Good> getLuxuryDemands() {
        return this.luxuryDemands;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PopulationType that)) return false;

        return name.equals(that.name);
    }

    @Override
    public String toString() {
        return "PopulationType{" +
            "color=" + this.color +
            ", name='" + this.name + '\'' +
            ", standardDemands=" + this.standardDemands +
            ", luxuryDemands=" + this.luxuryDemands +
            '}';
    }
}

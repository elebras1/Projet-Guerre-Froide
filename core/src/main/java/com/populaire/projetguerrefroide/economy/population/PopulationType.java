package com.populaire.projetguerrefroide.economy.population;

public class PopulationType {
    private final short id;
    private final float children;
    private final float adults;
    private final float seniors;

    public PopulationType(short id, float children, float adults, float seniors) {
        this.id = id;
        this.children = children;
        this.adults = adults;
        this.seniors = seniors;
    }

    public short getId() {
        return this.id;
    }

    public float getChildren() {
        return this.children;
    }

    public float getAdults() {
        return this.adults;
    }

    public float getSeniors() {
        return this.seniors;
    }

    @Override
    public String toString() {
        return "PopulationType{" +
                "id=" + this.id +
                "children=" + this.children +
                ", adults=" + this.adults +
                ", seniors=" + this.seniors +
                '}';
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PopulationType that = (PopulationType) obj;
        return this.id == that.id;
    }
}

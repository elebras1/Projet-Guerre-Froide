package com.populaire.projetguerrefroide.economy.good;

public class MilitaryGood extends Good {
    public MilitaryGood(String name, float cost, int color) {
        super(name, cost, color);
    }

    @Override
    public String toString() {
        return "MilitaryGood{" +
            "name='" + this.getName() + '\'' +
            ", cost=" + this.getCost() +
            ", color=" + this.getColor() +
            '}';
    }
}

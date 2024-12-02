package com.populaire.projetguerrefroide.economy.good;

public class AdvancedGood extends Good {
    public AdvancedGood(String name, float cost, int color) {
        super(name, cost, color);
    }

    @Override
    public String toString() {
        return "AdvancedGood{" +
            "name='" + this.getName() + '\'' +
            ", cost=" + this.getCost() +
            ", color=" + this.getColor() +
            '}';
    }
}

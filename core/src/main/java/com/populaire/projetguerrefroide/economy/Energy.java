package com.populaire.projetguerrefroide.economy;

public class Energy extends Good {
    public Energy(String name, float cost, int color) {
        super(name, cost, color);
    }

    @Override
    public String toString() {
        return "Energy{" +
            "name='" + this.getName() + '\'' +
            ", cost=" + this.getCost() +
            ", color=" + this.getColor() +
            '}';
    }
}

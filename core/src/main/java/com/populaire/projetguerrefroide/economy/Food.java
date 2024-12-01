package com.populaire.projetguerrefroide.economy;

public class Food extends Resource {
    public Food(String name, float production, float infraProduction, short basePopulation, short infraPopulation, float cost, int color) {
        super(name, production, infraProduction, basePopulation, infraPopulation, cost, color);
    }

    @Override
    public String toString() {
        return "Food{" +
            "name='" + this.getName() + '\'' +
            ", production=" + this.getProduction() +
            ", infraProduction=" + this.getInfraProduction() +
            ", basePopulation=" + this.getBasePopulation() +
            ", cost=" + this.getCost() +
            ", color=" + this.getColor() +
            '}';
    }
}

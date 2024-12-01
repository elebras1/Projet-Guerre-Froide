package com.populaire.projetguerrefroide.economy;

public class NaturalResource extends Resource {
    private final short priority;

    public NaturalResource(String name, float production, float infraProduction, short basePopulation, short infraPopulation, float cost, int color, short priority) {
        super(name, production, infraProduction, basePopulation, infraPopulation, cost, color);
        this.priority = priority;
    }

    public short getPriority() {
        return this.priority;
    }

    @Override
    public String toString() {
        return "NaturalRessource{" +
                "name='" + this.getName() + '\'' +
                ", production=" + this.getProduction() +
                ", infraProduction=" + this.getInfraProduction() +
                ", basePopulation=" + this.getBasePopulation() +
                ", cost=" + this.getCost() +
                ", color=" + this.getColor() +
                ", priority=" + this.priority +
                '}';
    }
}

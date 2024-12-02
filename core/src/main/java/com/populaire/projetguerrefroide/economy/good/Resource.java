package com.populaire.projetguerrefroide.economy.good;

public abstract class Resource extends Good {
    private final float production;
    private final float infraProduction;
    private final short basePopulation;
    private final short infraPopulation;

    public Resource(String name, float production, float infraProduction, short basePopulation, short infraPopulation, float cost, int color) {
        super(name, cost, color);
        this.production = production;
        this.infraProduction = infraProduction;
        this.basePopulation = basePopulation;
        this.infraPopulation = infraPopulation;
    }

    public float getProduction() {
        return this.production;
    }

    public float getInfraProduction() {
        return this.infraProduction;
    }

    public short getBasePopulation() {
        return this.basePopulation;
    }

    public short getInfraPopulation() {
        return this.infraPopulation;
    }
}

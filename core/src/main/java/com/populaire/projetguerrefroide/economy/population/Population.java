package com.populaire.projetguerrefroide.economy.population;

public class Population {
    private final int amount;
    private final PopulationType template;

    public Population(int amount, PopulationType template) {
        this.amount = amount;
        this.template = template;
    }

    public int getSize() {
        return this.amount;
    }

    public String toString() {
        return "Population{" +
                "amount=" + this.amount +
                ", template=" + this.template +
                '}';
    }
}

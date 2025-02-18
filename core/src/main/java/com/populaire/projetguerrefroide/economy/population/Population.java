package com.populaire.projetguerrefroide.economy.population;

import com.github.tommyettinger.ds.ObjectIntMap;

public class Population {
    private int amountChildren;
    private int amountAdults;
    private int amountSeniors;
    private final ObjectIntMap<PopulationType> populations;

    public Population(int amountChildren, int amountAdults, int amountSeniors, ObjectIntMap<PopulationType> populations) {
        this.amountChildren = amountChildren;
        this.amountAdults = amountAdults;
        this.amountSeniors = amountSeniors;
        this.populations = populations;
    }

    public int getAmountChildren() {
        return this.amountChildren;
    }

    public int getAmountAdults() {
        return this.amountAdults;
    }

    public int getAmountSeniors() {
        return this.amountSeniors;
    }

    public int getAmount() {
        return this.amountChildren + this.amountAdults + this.amountSeniors;
    }

    public ObjectIntMap<PopulationType> getPopulations() {
        return this.populations;
    }

    @Override
    public String toString() {
        return "Population{" +
            "amountChildren=" + this.amountChildren +
            ", amountAdults=" + this.amountAdults +
            ", amountSeniors=" + this.amountSeniors +
            '}';
    }
}

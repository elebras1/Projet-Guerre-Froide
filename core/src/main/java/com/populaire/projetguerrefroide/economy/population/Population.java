package com.populaire.projetguerrefroide.economy.population;

import com.github.tommyettinger.ds.ObjectIntMap;
import com.populaire.projetguerrefroide.national.Culture;
import com.populaire.projetguerrefroide.national.Religion;

public class Population {
    private int amountChildren;
    private int amountAdults;
    private int amountSeniors;
    private final ObjectIntMap<PopulationType> populations;
    private final ObjectIntMap<Culture> cultures;
    private final ObjectIntMap<Religion> religions;

    public Population(int amountChildren, int amountAdults, int amountSeniors, ObjectIntMap<PopulationType> populations, ObjectIntMap<Culture> cultures, ObjectIntMap<Religion> religions) {
        this.amountChildren = amountChildren;
        this.amountAdults = amountAdults;
        this.amountSeniors = amountSeniors;
        this.populations = populations;
        this.cultures = cultures;
        this.religions = religions;
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

    public ObjectIntMap<Culture> getCultures() {
        return this.cultures;
    }

    public ObjectIntMap<Religion> getReligions() {
        return this.religions;
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

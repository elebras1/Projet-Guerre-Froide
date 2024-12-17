package com.populaire.projetguerrefroide.economy;

import com.populaire.projetguerrefroide.economy.good.Good;

import java.util.Map;

public class PopulationDemands {
    private final short amount;
    private final Map<Good, Float> demands;

    public PopulationDemands(short amount, Map<Good, Float> demands) {
        this.amount = amount;
        this.demands = demands;
    }

    @Override
    public String toString() {
        return "PopulationDemands{" +
                "amount=" + this.amount +
                ", demands=" + this.demands +
                '}';
    }
}

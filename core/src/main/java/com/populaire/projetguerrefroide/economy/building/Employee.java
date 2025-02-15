package com.populaire.projetguerrefroide.economy.building;

import com.populaire.projetguerrefroide.economy.population.PopulationType;

public class Employee {
    private final PopulationType populationType;
    private final float amount;
    private final float effectMultiplier;

    public Employee(PopulationType populationType, float amount, float effectMultiplier) {
        this.populationType = populationType;
        this.amount = amount;
        this.effectMultiplier = effectMultiplier;
    }

    public PopulationType getPopulationType() {
        return this.populationType;
    }

    public float getAmount() {
        return this.amount;
    }

    public float getEffectMultiplier() {
        return this.effectMultiplier;
    }

    @Override
    public String toString() {
        return "BuildingTemplate{" +
            "populationType=" + this.populationType +
            ", amount=" + this.amount +
            ", effectMultiplier=" + this.effectMultiplier +
            '}';
    }
}

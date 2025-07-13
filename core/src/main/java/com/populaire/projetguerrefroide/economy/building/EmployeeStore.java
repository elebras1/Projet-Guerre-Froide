package com.populaire.projetguerrefroide.economy.building;

import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.IntList;

public class EmployeeStore {
    private final IntList populationTypeIds;
    private final FloatList amounts;
    private final FloatList effectMultipliers;

    public EmployeeStore(IntList populationTypeIds, FloatList amounts, FloatList effectMultipliers) {
        this.populationTypeIds = populationTypeIds;
        this.amounts = amounts;
        this.effectMultipliers = effectMultipliers;
    }

    public IntList getPopulationTypeIds() {
        return this.populationTypeIds;
    }

    public FloatList getAmounts() {
        return this.amounts;
    }

    public FloatList getEffectMultipliers() {
        return this.effectMultipliers;
    }

    public int getPopulationTypeId(int index) {
        return this.populationTypeIds.get(index);
    }

    public float getAmount(int index) {
        return this.amounts.get(index);
    }

    public float getEffectMultiplier(int index) {
        return this.effectMultipliers.get(index);
    }

    @Override
    public String toString() {
        return "Employee{" +
            "populationTypeIds=" + this.populationTypeIds +
            ", amounts=" + this.amounts +
            ", effectMultipliers=" + this.effectMultipliers +
            '}';
    }
}

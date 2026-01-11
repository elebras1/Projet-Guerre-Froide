package com.populaire.projetguerrefroide.economy.building;

import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.LongList;

public class EmployeeStore {
    private final LongList populationTypeIds;
    private final FloatList amounts;
    private final FloatList effectMultipliers;

    public EmployeeStore(LongList populationTypeIds, FloatList amounts, FloatList effectMultipliers) {
        this.populationTypeIds = populationTypeIds;
        this.amounts = amounts;
        this.effectMultipliers = effectMultipliers;
    }

    public LongList getPopulationTypeIds() {
        return this.populationTypeIds;
    }

    public FloatList getAmounts() {
        return this.amounts;
    }

    public FloatList getEffectMultipliers() {
        return this.effectMultipliers;
    }

    public long getPopulationTypeId(int index) {
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

package com.populaire.projetguerrefroide.dao.builder;

import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.LongList;
import com.populaire.projetguerrefroide.economy.building.EmployeeStore;

public class EmployeeStoreBuilder {
    private final int defaultCapacity;
    private int index;
    private final LongList populationTypeIds;
    private final FloatList amounts;
    private final FloatList effectMultipliers;

    public EmployeeStoreBuilder() {
        this.defaultCapacity = 10;
        this.index = 0;
        this.populationTypeIds = new LongList(this.defaultCapacity);
        this.amounts = new FloatList(this.defaultCapacity);
        this.effectMultipliers = new FloatList(this.defaultCapacity);
    }

    public int getDefaultCapacity() {
        return this.defaultCapacity;
    }

    public int getIndex() {
        return this.index;
    }

    public void addEmployee(long populationTypeId, float amount, float effectMultiplier) {
        this.populationTypeIds.add(populationTypeId);
        this.amounts.add(amount);
        this.effectMultipliers.add(effectMultiplier);
        this.index = this.populationTypeIds.size() - 1;
    }

    public EmployeeStore build() {
        return new EmployeeStore(this.populationTypeIds, this.amounts, this.effectMultipliers);
    }
}

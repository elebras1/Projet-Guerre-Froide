package com.populaire.projetguerrefroide.dao.builder;

import com.github.tommyettinger.ds.IntList;
import com.populaire.projetguerrefroide.economy.building.ProductionTypeStore;

public class ProductionTypeStoreBuilder {
    private final int defaultCapacity;
    private int index;
    private final IntList workforces;
    private final IntList ownerIds;
    private final IntList employeeIds;
    private final IntList employeeStarts;
    private final IntList employeeCounts;

    public ProductionTypeStoreBuilder() {
        this.defaultCapacity = 5;
        this.index = 0;
        this.workforces = new IntList(this.defaultCapacity);
        this.ownerIds = new IntList(this.defaultCapacity);
        this.employeeIds = new IntList();
        this.employeeStarts = new IntList(this.defaultCapacity);
        this.employeeCounts = new IntList(this.defaultCapacity);
    }

    public int getDefaultCapacity() {
        return this.defaultCapacity;
    }

    public int getIndex() {
        return this.index;
    }

    public ProductionTypeStoreBuilder addProductionType(int workforce, int ownerId) {
        this.workforces.add(workforce);
        this.ownerIds.add(ownerId);
        this.employeeStarts.add(this.employeeIds.size());
        this.employeeCounts.add(0);
        this.index = this.workforces.size() - 1;
        return this;
    }

    public void addEmployee(int employeeId) {
        this.employeeIds.add(employeeId);
        int currentCount = this.employeeCounts.get(this.index);
        this.employeeCounts.set(this.index, currentCount + 1);
    }

    public ProductionTypeStore build() {
        return new ProductionTypeStore(this.workforces, this.ownerIds, this.employeeIds, this.employeeStarts, this.employeeCounts);
    }

}

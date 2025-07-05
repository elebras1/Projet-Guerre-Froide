package com.populaire.projetguerrefroide.economy.building;

import com.github.tommyettinger.ds.IntList;

public class ProductionTypeStore {
    private final IntList workforces;
    private final IntList ownerIds;
    private final IntList employeeIds;
    private final IntList employeeStarts;
    private final IntList employeeCounts;

    public ProductionTypeStore(IntList workforces, IntList ownerIds, IntList employeeIds, IntList employeeStarts, IntList employeeCounts) {
        this.workforces = workforces;
        this.ownerIds = ownerIds;
        this.employeeIds = employeeIds;
        this.employeeStarts = employeeStarts;
        this.employeeCounts = employeeCounts;
    }

    public IntList getWorkforces() {
        return this.workforces;
    }

    public IntList getOwnerIds() {
        return this.ownerIds;
    }

    public IntList getEmployeeIds() {
        return this.employeeIds;
    }

    public IntList getEmployeeStarts() {
        return this.employeeStarts;
    }

    public IntList getEmployeeCounts() {
        return this.employeeCounts;
    }

    @Override
    public String toString() {
        return "ProductionTypeStore{" +
                "workforces=" + this.workforces +
                ", ownerIds=" + this.ownerIds +
                ", employeeIds=" + this.employeeIds +
                ", employeeStarts=" + this.employeeStarts +
                ", employeeCounts=" + this.employeeCounts +
                '}';
    }
}

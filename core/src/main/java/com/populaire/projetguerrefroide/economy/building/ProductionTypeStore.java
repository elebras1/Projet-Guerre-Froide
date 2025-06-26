package com.populaire.projetguerrefroide.economy.building;

import com.github.tommyettinger.ds.IntList;

import java.util.List;

public class ProductionTypeStore {
    private final IntList workforces;
    private final IntList ownerIds;
    private final List<IntList> employeeIds;

    public ProductionTypeStore(IntList workforces, IntList ownerIds, List<IntList> employeeIds) {
        this.workforces = workforces;
        this.ownerIds = ownerIds;
        this.employeeIds = employeeIds;
    }

    public IntList getWorkforces() {
        return this.workforces;
    }

    public IntList getOwnerIds() {
        return this.ownerIds;
    }

    public List<IntList> getEmployeeIds() {
        return this.employeeIds;
    }


    @Override
    public String toString() {
        return "ResourceProductionType{" +
            "workforces=" + this.workforces +
            ", ownerIds=" + this.ownerIds +
            ", employeeIds=" + this.employeeIds +
            '}';
    }
}

package com.populaire.projetguerrefroide.economy.good;

import com.github.tommyettinger.ds.IntList;
import com.populaire.projetguerrefroide.economy.building.ProductionTypeStore;

import java.util.List;

public class ResourceProductionTypeStore extends ProductionTypeStore {
    public ResourceProductionTypeStore(IntList workforces, IntList ownerIds, List<IntList> employeeIds) {
        super(workforces, ownerIds, employeeIds);
    }

    @Override
    public String toString() {
        return "ResourceProductionType{" +
            "workforces=" + this.getWorkforces() +
            ", ownerIds=" + this.getOwnerIds() +
            ", employeeIds=" + this.getEmployeeIds() +
            '}';
    }
}

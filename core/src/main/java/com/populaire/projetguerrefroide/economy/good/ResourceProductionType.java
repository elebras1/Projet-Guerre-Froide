package com.populaire.projetguerrefroide.economy.good;

import com.populaire.projetguerrefroide.economy.building.Employee;
import com.populaire.projetguerrefroide.economy.building.ProductionType;
import com.populaire.projetguerrefroide.economy.population.PopulationType;

import java.util.List;

public class ResourceProductionType extends ProductionType {
    public ResourceProductionType(short workforce, PopulationType owner, List<Employee> employees) {
        super(workforce, owner, employees);
    }

    @Override
    public String toString() {
        return "ResourceProductionType{" +
            "workforce=" + this.getWorkforce() +
            ", owner=" + this.getOwner() +
            ", employees=" + this.getEmployees() +
            '}';
    }
}

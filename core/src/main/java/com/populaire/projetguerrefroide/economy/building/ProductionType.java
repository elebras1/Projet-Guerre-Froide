package com.populaire.projetguerrefroide.economy.building;

import com.populaire.projetguerrefroide.economy.population.PopulationType;

import java.util.List;

public class ProductionType {
    private final int workforce;
    private final PopulationType owner;
    private final List<Employee> employees;

    public ProductionType(int workforce, PopulationType owner, List<Employee> employees) {
        this.workforce = workforce;
        this.owner = owner;
        this.employees = employees;
    }

    public int getWorkforce() {
        return this.workforce;
    }

    public PopulationType getOwner() {
        return this.owner;
    }

    public List<Employee> getEmployees() {
        return this.employees;
    }

    @Override
    public String toString() {
        return "ProductionType{" +
            "workforce=" + this.workforce +
            ", owner=" + this.owner +
            ", employees=" + this.employees +
            '}';
    }
}

package com.populaire.projetguerrefroide.economy.building;

import com.populaire.projetguerrefroide.economy.population.PopulationType;

import java.util.List;

public class BuildingTemplate {
    private short workforce;
    private PopulationType owner;
    private List<Employee> employees;

    public BuildingTemplate(short workforce, PopulationType owner, List<Employee> employees) {
        this.workforce = workforce;
        this.owner = owner;
        this.employees = employees;
    }

    public short getWorkforce() {
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
        return "BuildingTemplate{" +
            "workforce=" + this.workforce +
            ", owner=" + this.owner +
            ", employees=" + this.employees +
            '}';
    }
}

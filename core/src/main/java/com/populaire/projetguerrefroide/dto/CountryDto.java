package com.populaire.projetguerrefroide.dto;

public class CountryDto {
    private final String population;
    private final int manpower;
    private final String grossDomesticProduct;
    private final int money;
    private final int supplies;
    private final int fuel;
    private final float diplomaticInfluence;
    private final int uranium;
    private final String dissent;
    private final String nationalUnity;

    public CountryDto(String population, int manpower, String grossDomesticProduct, int money, int supplies, int fuel, float diplomaticInfluence, int uranium, String dissent, String nationalUnity) {
        this.population = population;
        this.manpower = manpower;
        this.grossDomesticProduct = grossDomesticProduct;
        this.money = money;
        this.supplies = supplies;
        this.fuel = fuel;
        this.diplomaticInfluence = diplomaticInfluence;
        this.uranium = uranium;
        this.dissent = dissent;
        this.nationalUnity = nationalUnity;
    }

    public String getPopulation() {
        return this.population;
    }

    public int getManpower() {
        return this.manpower;
    }

    public String getGrossDomesticProduct() {
        return this.grossDomesticProduct;
    }

    public int getMoney() {
        return this.money;
    }

    public int getSupplies() {
        return this.supplies;
    }

    public int getFuel() {
        return this.fuel;
    }

    public float getDiplomaticInfluence() {
        return this.diplomaticInfluence;
    }

    public int getUranium() {
        return this.uranium;
    }

    public String getDissent() {
        return this.dissent;
    }

    public String getNationalUnity() {
        return this.nationalUnity;
    }
}

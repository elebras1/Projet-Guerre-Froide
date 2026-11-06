package com.populaire.projetguerrefroide.protocol.response;

import com.populaire.projetguerrefroide.protocol.request.Response;

public final class CountryOverview extends Response {
    private int countryIndex;
    private int population;
    private int manpower;
    private long grossDomesticProduct;
    private long money;
    private int supplies;
    private int fuel;
    private float diplomaticInfluence;
    private int uranium;
    private int dissent;
    private int nationalUnity;
    private int ranking;

    public void set(int countryIndex, int population, int manpower, long grossDomesticProduct, long money, int supplies, int fuel, float diplomaticInfluence, int uranium, int dissent, int nationalUnity, int ranking) {
        this.countryIndex = countryIndex;
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
        this.ranking = ranking;
    }

    public int countryIndex() {
        return this.countryIndex;
    }

    public int population() {
        return this.population;
    }

    public int manpower() {
        return this.manpower;
    }

    public long grossDomesticProduct() {
        return this.grossDomesticProduct;
    }

    public long money() {
        return this.money;
    }

    public int supplies() {
        return this.supplies;
    }

    public int fuel() {
        return this.fuel;
    }

    public float diplomaticInfluence() {
        return this.diplomaticInfluence;
    }

    public int uranium() {
        return this.uranium;
    }

    public int dissent() {
        return this.dissent;
    }

    public int nationalUnity() {
        return this.nationalUnity;
    }

    public int ranking() {
        return this.ranking;
    }
}

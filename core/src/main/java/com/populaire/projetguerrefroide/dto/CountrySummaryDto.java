package com.populaire.projetguerrefroide.dto;

public class CountrySummaryDto {
    private final String countryName;
    private final String idCountry;
    private final String population;
    private final String government;
    private final String portrait;
    private final String leaderFullName;

    public CountrySummaryDto(String countryName, String idCountry, String population, String government, String portrait, String leaderFullName) {
        this.countryName = countryName;
        this.idCountry = idCountry;
        this.population = population;
        this.government = government;
        this.portrait = portrait;
        this.leaderFullName = leaderFullName;
    }

    public String getCountryName() {
        return this.countryName;
    }

    public String getIdCountry() {
        return this.idCountry;
    }

    public String getPopulation() {
        return this.population;
    }

    public String getGovernment() {
        return this.government;
    }

    public String getPortrait() {
        return this.portrait;
    }

    public String getLeaderFullName() {
        return this.leaderFullName;
    }
}

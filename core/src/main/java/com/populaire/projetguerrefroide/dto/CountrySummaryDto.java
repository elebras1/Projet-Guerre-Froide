package com.populaire.projetguerrefroide.dto;

import java.util.List;

public class CountrySummaryDto {
    private final String countryName;
    private final String idCountry;
    private final String population;
    private final String government;
    private final String portrait;
    private final String leaderFullName;
    private final String colonizerId;
    private final List<String> allies;

    public CountrySummaryDto(String countryName, String idCountry, String population, String government, String portrait, String leaderFullName, String colonizerId, List<String> allies) {
        this.countryName = countryName;
        this.idCountry = idCountry;
        this.population = population;
        this.government = government;
        this.portrait = portrait;
        this.leaderFullName = leaderFullName;
        this.colonizerId = colonizerId;
        this.allies = allies;
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

    public String getColonizerId() {
        return this.colonizerId;
    }

    public List<String> getAllies() {
        return this.allies;
    }
}

package com.populaire.projetguerrefroide.dto;

import java.util.List;

public class ProvinceDto {
    private final String provinceId;
    private final String regionId;
    private final String terrainImage;
    private final String resourceImage;
    private final String populationRegion;
    private final String workersRegion;
    private final int developmentIndexRegion;
    private final int incomeRegion;
    private final int industryRegion;
    private final String flagImage;
    private final List<String> flagCountriesCore;
    private final float resourceProduced;
    private final int infrastructureValue;
    private final int guerillaValue;

    public ProvinceDto(String provinceId, String regionId, String terrainImage, String resourceImage, String populationRegion, String workersRegion, int developmentIndexRegion, int incomeRegion, int industryRegion, String flagImage, List<String> flagCountriesCore, float resourceProduced, int infrastructureValue, int guerillaValue) {
        this.provinceId = provinceId;
        this.regionId = regionId;
        this.terrainImage = terrainImage;
        this.resourceImage = resourceImage;
        this.populationRegion = populationRegion;
        this.workersRegion = workersRegion;
        this.developmentIndexRegion = developmentIndexRegion;
        this.incomeRegion = incomeRegion;
        this.industryRegion = industryRegion;
        this.flagImage = flagImage;
        this.flagCountriesCore = flagCountriesCore;
        this.resourceProduced = resourceProduced;
        this.infrastructureValue = infrastructureValue;
        this.guerillaValue = guerillaValue;
    }

    public String getProvinceId() {
        return this.provinceId;
    }

    public String getRegionId() {
        return this.regionId;
    }

    public String getTerrainImage() {
        return this.terrainImage;
    }

    public String getResourceImage() {
        return this.resourceImage;
    }

    public String getPopulationRegion() {
        return this.populationRegion;
    }

    public String getWorkersRegion() {
        return this.workersRegion;
    }

    public int getDevelopmentIndexRegion() {
        return this.developmentIndexRegion;
    }

    public int getIncomeRegion() {
        return this.incomeRegion;
    }

    public int getIndustryRegion() {
        return this.industryRegion;
    }

    public String getFlagImage() {
        return this.flagImage;
    }

    public List<String> getFlagCountriesCore() {
        return this.flagCountriesCore;
    }

    public float getResourceProduced() {
        return this.resourceProduced;
    }

    public int getInfrastructureValue() {
        return this.infrastructureValue;
    }

    public int getGuerillaValue() {
        return this.guerillaValue;
    }
}

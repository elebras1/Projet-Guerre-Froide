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
    private final String countryId;
    private final String colonizerId;
    private final List<String> flagCountriesCore;
    private final float resourceProduced;
    private final int infrastructureValue;
    private final int guerillaValue;
    private final String populationProvince;
    private final float incomeProvince;
    private final float revoltRisk;
    private final List<String> provinceIdsRegion;
    private final DevelopementBuildingLevelDto developmentBuildingLevel;
    private final List<String> specialBuildings;
    private final List<String> colorBuildings;

    public ProvinceDto(String provinceId, String regionId, String terrainImage, String resourceImage, String populationRegion, String workersRegion, int developmentIndexRegion, int incomeRegion, int industryRegion, String countryId, String colonizerId, List<String> flagCountriesCore, float resourceProduced, int infrastructureValue, int guerillaValue, String populationProvince, float incomeProvince, float revoltRisk, List<String> provinceIdsRegion, DevelopementBuildingLevelDto developmentBuildingLeve, List<String> specialBuildings, List<String> colorBuildings) {
        this.provinceId = provinceId;
        this.regionId = regionId;
        this.terrainImage = terrainImage;
        this.resourceImage = resourceImage;
        this.populationRegion = populationRegion;
        this.workersRegion = workersRegion;
        this.developmentIndexRegion = developmentIndexRegion;
        this.incomeRegion = incomeRegion;
        this.industryRegion = industryRegion;
        this.countryId = countryId;
        this.colonizerId = colonizerId;
        this.flagCountriesCore = flagCountriesCore;
        this.resourceProduced = resourceProduced;
        this.infrastructureValue = infrastructureValue;
        this.guerillaValue = guerillaValue;
        this.populationProvince = populationProvince;
        this.incomeProvince = incomeProvince;
        this.revoltRisk = revoltRisk;
        this.provinceIdsRegion = provinceIdsRegion;
        this.developmentBuildingLevel = developmentBuildingLeve;
        this.specialBuildings = specialBuildings;
        this.colorBuildings = colorBuildings;
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

    public String getCountryId() {
        return this.countryId;
    }

    public String getColonizerId() {
        return this.colonizerId;
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

    public String getPopulationProvince() {
        return this.populationProvince;
    }

    public float getIncomeProvince() {
        return this.incomeProvince;
    }

    public float getRevoltRisk() {
        return this.revoltRisk;
    }

    public List<String> getProvinceIdsRegion() {
        return this.provinceIdsRegion;
    }

    public DevelopementBuildingLevelDto getDevelopmentBuildingLevel() {
        return this.developmentBuildingLevel;
    }

    public List<String> getSpecialBuildings() {
        return this.specialBuildings;
    }

    public List<String> getColorsBuildings() {
        return this.colorBuildings;
    }
}

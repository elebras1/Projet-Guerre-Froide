package com.populaire.projetguerrefroide.dto;

import java.util.List;

public record ProvinceDto(String provinceId, String regionId, String terrainImage, String resourceImage, int populationRegion, int workersRegion, int developmentIndexRegion, int incomeRegion, int industryRegion, String countryId, String colonizerId, List<String> flagCountriesCore, float resourceProduced, int infrastructureValue, int guerillaValue, int populationProvince, float incomeProvince, float revoltRisk, List<String> provinceIdsRegion, DevelopementBuildingLevelDto developmentBuildingLevel, List<String> specialBuildings, List<String> colorBuildings) {
}

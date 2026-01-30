package com.populaire.projetguerrefroide.dto;

import java.util.List;

public record RegionDto(String regionId, int populationAmount, int buildingWorkerAmount, int buildingWorkerRatio, byte developpementIndexValue, List<BuildingSummaryDto> buildings) {
}

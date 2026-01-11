package com.populaire.projetguerrefroide.dto;

import java.util.List;
import java.util.Map;

public record RegionsBuildingsDto(Map<RegionDto, List<BuildingDto>> regionsBuildings) {
}

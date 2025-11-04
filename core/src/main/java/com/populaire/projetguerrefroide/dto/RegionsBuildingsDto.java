package com.populaire.projetguerrefroide.dto;

import java.util.List;

public class RegionsBuildingsDto {
    private final List<String> regionIds;

    public RegionsBuildingsDto(List<String> regionIds) {
        this.regionIds = regionIds;
    }

    public List<String> getRegionIds() {
        return this.regionIds;
    }
}

package com.populaire.projetguerrefroide.dto;

import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.ObjectIntMap;

import java.util.List;

public class RegionsBuildingsDto {
    private final List<String> regionIds;
    private final ObjectIntMap<String> regionIndices;
    private final IntList buildingIds;
    private final IntList buildingValues;
    private final IntList buildingStarts;
    private final IntList buildingCounts;


    public RegionsBuildingsDto(List<String> regionIds, ObjectIntMap<String> regionIndices, IntList buildingIds, IntList buildingValues, IntList buildingStarts, IntList buildingCounts) {
        this.regionIds = regionIds;
        this.regionIndices = regionIndices;
        this.buildingIds = buildingIds;
        this.buildingValues = buildingValues;
        this.buildingStarts = buildingStarts;
        this.buildingCounts = buildingCounts;
    }

    public List<String> getRegionIds() {
        return this.regionIds;
    }

    public ObjectIntMap<String> getRegionIndices() {
        return this.regionIndices;
    }

    public IntList getBuildingIds() {
        return this.buildingIds;
    }

    public IntList getBuildingValues() {
        return this.buildingValues;
    }

    public IntList getBuildingStarts() {
        return this.buildingStarts;
    }

    public IntList getBuildingCounts() {
        return this.buildingCounts;
    }


}

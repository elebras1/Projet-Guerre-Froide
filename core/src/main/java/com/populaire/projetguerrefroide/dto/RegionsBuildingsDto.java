package com.populaire.projetguerrefroide.dto;

import com.github.tommyettinger.ds.ByteList;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.ObjectIntMap;

import java.util.List;

public class RegionsBuildingsDto {
    private final List<String> regionIds;
    private final ObjectIntMap<String> regionIdLookup;
    private final IntList buildingIds;
    private final IntList buildingValues;
    private final IntList buildingStarts;
    private final IntList buildingCounts;
    private final List<String> buildingNames;
    private final ByteList buildingTypes;


    public RegionsBuildingsDto(List<String> regionIds, ObjectIntMap<String> regionIdLookup, IntList buildingIds, IntList buildingValues, IntList buildingStarts, IntList buildingCounts, List<String> buildingNames, ByteList buildingTypes) {
        this.regionIds = regionIds;
        this.regionIdLookup = regionIdLookup;
        this.buildingIds = buildingIds;
        this.buildingValues = buildingValues;
        this.buildingStarts = buildingStarts;
        this.buildingCounts = buildingCounts;
        this.buildingNames = buildingNames;
        this.buildingTypes = buildingTypes;
    }

    public List<String> getRegionIds() {
        return this.regionIds;
    }

    public ObjectIntMap<String> getRegionIdLookup() {
        return this.regionIdLookup;
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

    public List<String> getBuildingNames() {
        return this.buildingNames;
    }

    public ByteList getBuildingTypes() {
        return this.buildingTypes;
    }

}

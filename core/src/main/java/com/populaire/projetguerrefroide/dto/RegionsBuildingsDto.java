package com.populaire.projetguerrefroide.dto;

import com.github.tommyettinger.ds.ByteList;
import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.ObjectIntMap;

import java.util.List;
import java.util.Set;

public class RegionsBuildingsDto {
    private final Set<String> regionIds;
    private final IntList regionInternalIds;
    private final ObjectIntMap<String> regionIdLookup;
    private final IntList buildingIds;
    private final IntList buildingValues;
    private final IntList buildingStarts;
    private final IntList buildingCounts;
    private final List<String> buildingNames;
    private final ByteList buildingTypes;
    private final ByteList buildingMaxLevels;
    private final ByteList developpementIndexValues;
    private final IntList populationAmounts;
    private final IntList buildingWorkersAmount;
    private final ByteList buildingWorkersRatio;
    private final FloatList buildingProductionValues;

    public RegionsBuildingsDto(Set<String> regionIds, IntList regionInternalIds, ObjectIntMap<String> regionIdLookup, IntList buildingIds, IntList buildingValues, IntList buildingStarts, IntList buildingCounts, List<String> buildingNames, ByteList buildingTypes, ByteList buildingMaxLevels, ByteList developpementIndexValues, IntList populationAmounts, IntList buildingWorkersAmount, ByteList buildingWorkersRatio, FloatList buildingProductionValues) {
        this.regionIds = regionIds;
        this.regionInternalIds = regionInternalIds;
        this.regionIdLookup = regionIdLookup;
        this.buildingIds = buildingIds;
        this.buildingValues = buildingValues;
        this.buildingStarts = buildingStarts;
        this.buildingCounts = buildingCounts;
        this.buildingNames = buildingNames;
        this.buildingTypes = buildingTypes;
        this.buildingMaxLevels = buildingMaxLevels;
        this.developpementIndexValues = developpementIndexValues;
        this.populationAmounts = populationAmounts;
        this.buildingWorkersAmount = buildingWorkersAmount;
        this.buildingWorkersRatio = buildingWorkersRatio;
        this.buildingProductionValues = buildingProductionValues;
    }

    public Set<String> getRegionIds() {
        return this.regionIds;
    }

    public IntList getRegionInternalIds() {
        return this.regionInternalIds;
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

    public ByteList getBuildingMaxLevels() {
        return this.buildingMaxLevels;
    }

    public ByteList getDeveloppementIndexValues() {
        return this.developpementIndexValues;
    }

    public IntList getPopulationAmounts() {
        return this.populationAmounts;
    }

    public IntList getBuildingWorkersAmount() {
        return this.buildingWorkersAmount;
    }

    public ByteList getBuildingWorkersRatio() {
        return this.buildingWorkersRatio;
    }

    public FloatList getBuildingProductionValues() {
        return this.buildingProductionValues;
    }
}

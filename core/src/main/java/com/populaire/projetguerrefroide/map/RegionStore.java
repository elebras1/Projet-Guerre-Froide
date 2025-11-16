package com.populaire.projetguerrefroide.map;

import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.ObjectIntMap;

public class RegionStore {
    private final ObjectIntMap<String> regionIds;
    private final IntList buildingIds;
    private final IntList buildingValues;
    private final IntList buildingStarts;
    private final IntList buildingCounts;
    private final FloatList buildingProductionValues;
    private final IntList buildingWorkersAmountValues;

    public RegionStore(ObjectIntMap<String> regionIds, IntList buildingIds, IntList buildingValues, IntList buildingStarts, IntList buildingCounts) {
        this.regionIds = regionIds;
        this.buildingIds = buildingIds;
        this.buildingValues = buildingValues;
        this.buildingStarts = buildingStarts;
        this.buildingCounts = buildingCounts;
        this.buildingProductionValues = new FloatList(buildingIds.size());
        this.buildingProductionValues.setSize(buildingIds.size());
        this.buildingWorkersAmountValues = new IntList(buildingIds.size());
        this.buildingWorkersAmountValues.setSize(buildingIds.size());
    }

    public ObjectIntMap<String> getRegionIds() {
        return this.regionIds;
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

    public FloatList getBuildingProductionValues() {
        return this.buildingProductionValues;
    }

    public IntList getBuildingWorkersAmountValues() {
        return this.buildingWorkersAmountValues;
    }

    @Override
    public String toString() {
        return "RegionStore{" +
                "regionIds=" + this.regionIds +
                "buildingIds=" + this.buildingIds +
                ", buildingValues=" + this.buildingValues +
                ", buildingStarts=" + this.buildingStarts +
                ", buildingCounts=" + this.buildingCounts +
                '}';
    }

}

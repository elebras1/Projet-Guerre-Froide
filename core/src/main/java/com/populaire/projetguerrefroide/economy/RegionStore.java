package com.populaire.projetguerrefroide.economy;

import com.github.tommyettinger.ds.IntList;

public class RegionStore {
    private final IntList buildingIds;
    private final IntList buildingValues;
    private final IntList buildingStart;
    private final IntList buildingCount;

    public RegionStore(IntList buildingIds, IntList buildingValues, IntList buildingStart, IntList buildingCount) {
        this.buildingIds = buildingIds;
        this.buildingValues = buildingValues;
        this.buildingStart = buildingStart;
        this.buildingCount = buildingCount;
    }

    public IntList getBuildingIds() {
        return this.buildingIds;
    }

    public IntList getBuildingValues() {
        return this.buildingValues;
    }

    public IntList getBuildingStart() {
        return this.buildingStart;
    }

    public IntList getBuildingCount() {
        return this.buildingCount;
    }

    @Override
    public String toString() {
        return "RegionStore{" +
                "buildingIds=" + this.buildingIds +
                ", buildingValues=" + this.buildingValues +
                ", buildingStart=" + this.buildingStart +
                ", buildingCount=" + this.buildingCount +
                '}';
    }

}

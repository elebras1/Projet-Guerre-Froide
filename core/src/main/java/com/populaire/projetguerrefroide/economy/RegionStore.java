package com.populaire.projetguerrefroide.economy;

import com.github.tommyettinger.ds.IntList;

public class RegionStore {
    private final IntList buildingIds;
    private final IntList buildingValues;
    private final IntList buildingStarts;
    private final IntList buildingCounts;

    public RegionStore(IntList buildingIds, IntList buildingValues, IntList buildingStarts, IntList buildingCounts) {
        this.buildingIds = buildingIds;
        this.buildingValues = buildingValues;
        this.buildingStarts = buildingStarts;
        this.buildingCounts = buildingCounts;
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

    @Override
    public String toString() {
        return "RegionStore{" +
                "buildingIds=" + this.buildingIds +
                ", buildingValues=" + this.buildingValues +
                ", buildingStarts=" + this.buildingStarts +
                ", buildingCounts=" + this.buildingCounts +
                '}';
    }

}

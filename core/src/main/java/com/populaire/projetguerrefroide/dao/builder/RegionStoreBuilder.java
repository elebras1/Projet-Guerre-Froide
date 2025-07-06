package com.populaire.projetguerrefroide.dao.builder;

import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.ObjectIntMap;
import com.populaire.projetguerrefroide.map.RegionStore;

public class RegionStoreBuilder {
    private final int defaultCapacity;
    private int index;
    private final ObjectIntMap<String> regionIds;
    private final IntList buildingIds;
    private final IntList buildingValues;
    private final IntList buildingStarts;
    private final IntList buildingCounts;

    public RegionStoreBuilder() {
        this.defaultCapacity = 396;
        this.index = 0;
        this.regionIds = new ObjectIntMap<>(this.defaultCapacity);
        this.buildingIds = new IntList(this.defaultCapacity);
        this.buildingValues = new IntList(this.defaultCapacity);
        this.buildingStarts = new IntList(this.defaultCapacity);
        this.buildingCounts = new IntList(this.defaultCapacity);
    }

    public int getDefaultCapacity() {
        return this.defaultCapacity;
    }

    public int getIndex() {
        return this.index;
    }

    public RegionStoreBuilder addRegion(String regionId) {
        this.regionIds.put(regionId, this.index);
        this.buildingStarts.add(this.buildingIds.size());
        this.buildingCounts.add(0);
        this.index = this.regionIds.size() - 1;
        return this;
    }

    public RegionStoreBuilder addBuilding(int buildingId, int value) {
        int startIndex = this.buildingStarts.get(this.index);
        int endIndex = startIndex + this.buildingCounts.get(this.index);
        for(int i = startIndex; i < endIndex; i++) {
            if (this.buildingIds.get(i) == buildingId) {
                this.buildingValues.set(i, this.buildingValues.get(i) + value);
                return this;
            }
        }

        this.buildingIds.add(buildingId);
        this.buildingValues.add(value);
        int currentCount = this.buildingCounts.get(this.index);
        this.buildingCounts.set(this.index, currentCount + 1);
        return this;
    }

    public RegionStore build() {
        return new RegionStore(this.regionIds, this.buildingIds, this.buildingValues, this.buildingStarts, this.buildingCounts);
    }
}

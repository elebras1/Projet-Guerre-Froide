package com.populaire.projetguerrefroide.dao.builder;

import com.github.tommyettinger.ds.*;
import com.populaire.projetguerrefroide.economy.building.BuildingStore;

import java.util.List;

public class BuildingStoreBuilder {
    private final int defaultCapacity;
    private int index;
    private final List<String> names;
    private final IntList times;
    private final ByteList types;
    private final LongList goodsCostGoodIds;
    private final FloatList goodsCostValues;
    private final IntList goodsCostStarts;
    private final IntList goodsCostCounts;
    private final ByteList maxLevels;
    private final IntList modifierIds;
    private final IntList modifierStart;
    private final IntList modifierCount;
    private final IntList baseTypeIds;
    private final IntList artisansTypeIds;
    private final LongList inputGoodIds;
    private final FloatList inputGoodValues;
    private final IntList inputGoodStarts;
    private final IntList inputGoodCounts;
    private final LongList outputGoodIds;
    private final FloatList outputGoodValues;
    private final IntList costs;
    private final BooleanList onMap;

    public BuildingStoreBuilder() {
        this.defaultCapacity = 54;
        this.index = 0;
        this.names = new ObjectList<>(this.defaultCapacity);
        this.times = new IntList(this.defaultCapacity);
        this.types = new ByteList(this.defaultCapacity);
        this.goodsCostGoodIds = new LongList();
        this.goodsCostValues = new FloatList();
        this.goodsCostStarts = new IntList(this.defaultCapacity);
        this.goodsCostCounts = new IntList(this.defaultCapacity);
        this.maxLevels = new ByteList(this.defaultCapacity);
        this.modifierIds = new IntList();
        this.modifierStart = new IntList(this.defaultCapacity);
        this.modifierCount = new IntList(this.defaultCapacity);
        this.baseTypeIds = new IntList();
        this.artisansTypeIds = new IntList();
        this.inputGoodIds = new LongList();
        this.inputGoodValues = new FloatList();
        this.inputGoodStarts = new IntList(this.defaultCapacity);
        this.inputGoodCounts = new IntList(this.defaultCapacity);
        this.outputGoodIds = new LongList();
        this.outputGoodValues = new FloatList();
        this.costs = new IntList();
        this.onMap = new BooleanList();
    }

    public int getDefaultCapacity() {
        return this.defaultCapacity;
    }

    public int getIndex() {
        return this.index;
    }

    public BuildingStoreBuilder addBuilding(String name, int time, byte type) {
        this.names.add(name);
        this.times.add(time);
        this.types.add(type);

        this.goodsCostStarts.add(this.goodsCostGoodIds.size());
        this.goodsCostCounts.add(0);
        this.maxLevels.add((byte) 0);

        this.modifierStart.add(this.modifierIds.size());
        this.modifierCount.add(0);

        this.inputGoodStarts.add(this.inputGoodIds.size());
        this.inputGoodCounts.add(0);

        this.index = this.names.size() - 1;
        return this;
    }

    public BuildingStoreBuilder addGoodsCost(long goodId, float value) {
        this.goodsCostGoodIds.add(goodId);
        this.goodsCostValues.add(value);

        int currentCount = this.goodsCostCounts.get(this.index);
        this.goodsCostCounts.set(this.index, currentCount + 1);

        return this;
    }

    public BuildingStoreBuilder addMaxLevel(byte maxLevel) {
        this.maxLevels.set(this.index, maxLevel);
        return this;
    }

    public BuildingStoreBuilder addModifier(int modifierId) {
        this.modifierIds.add(modifierId);

        int currentCount = this.modifierCount.get(this.index);
        this.modifierCount.set(this.index, currentCount + 1);

        return this;
    }

    public BuildingStoreBuilder addBaseType(int baseTypeId) {
        this.baseTypeIds.add(baseTypeId);
        return this;
    }

    public BuildingStoreBuilder addArtisansType(int artisansTypeId) {
        this.artisansTypeIds.add(artisansTypeId);
        return this;
    }

    public BuildingStoreBuilder addInputGood(long goodId, float value) {
        this.inputGoodIds.add(goodId);
        this.inputGoodValues.add(value);

        int currentCount = this.inputGoodCounts.get(this.index);
        this.inputGoodCounts.set(this.index, currentCount + 1);

        return this;
    }

    public BuildingStoreBuilder addOutputGood(long goodId, float value) {
        this.outputGoodIds.add(goodId);
        this.outputGoodValues.add(value);
        return this;
    }

    public BuildingStoreBuilder addCost(int cost) {
        this.costs.add(cost);
        return this;
    }

    public BuildingStoreBuilder addOnMap(boolean onMap) {
        this.onMap.add(onMap);
        return this;
    }

    public BuildingStore build() {
        return new BuildingStore(this.names, this.times, this.types, this.goodsCostGoodIds, this.goodsCostValues, this.goodsCostStarts, this.goodsCostCounts, this.maxLevels, this.modifierIds, this.modifierStart, this.modifierCount, this.baseTypeIds, this.artisansTypeIds, this.inputGoodIds, this.inputGoodValues, this.inputGoodStarts, this.inputGoodCounts, this.outputGoodIds, this.outputGoodValues, this.costs, this.onMap);
    }
}

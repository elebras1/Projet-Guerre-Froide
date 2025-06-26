package com.populaire.projetguerrefroide.economy.building;

import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.IntList;

import java.util.List;

public class EconomyBuildingStore extends BuildingStore {
    private final IntList baseTypeIds;
    private final IntList artisansTypeIds;
    private final IntList maxLevels;
    private final IntList inputGoodIds;
    private final FloatList inputGoodValues;
    private final IntList inputGoodStart;
    private final IntList inputGoodCount;
    private final IntList outputGoodIds;
    private final FloatList outputGoodValues;
    private final IntList outputGoodStart;
    private final IntList outputGoodCount;

    public EconomyBuildingStore(List<String> names, IntList times, IntList types, IntList onMapFlags, IntList goodsCostGoodIds, FloatList goodsCostValues, IntList goodsCostStart, IntList goodsCostCount, IntList baseTypeIds, IntList artisansTypeIds, IntList maxLevels, IntList inputGoodIds, FloatList inputGoodValues, IntList inputGoodStart, IntList inputGoodCount, IntList outputGoodIds, FloatList outputGoodValues, IntList outputGoodStart, IntList outputGoodCount) {
        super(names, times, types, onMapFlags, goodsCostGoodIds, goodsCostValues, goodsCostStart, goodsCostCount);
        this.baseTypeIds = baseTypeIds;
        this.artisansTypeIds = artisansTypeIds;
        this.maxLevels = maxLevels;
        this.inputGoodIds = inputGoodIds;
        this.inputGoodValues = inputGoodValues;
        this.inputGoodStart = inputGoodStart;
        this.inputGoodCount = inputGoodCount;
        this.outputGoodIds = outputGoodIds;
        this.outputGoodValues = outputGoodValues;
        this.outputGoodStart = outputGoodStart;
        this.outputGoodCount = outputGoodCount;
    }

    public IntList getBaseTypeIds() {
        return this.baseTypeIds;
    }

    public IntList getArtisansTypeIds() {
        return this.artisansTypeIds;
    }

    public IntList getMaxLevels() {
        return this.maxLevels;
    }

    public IntList getInputGoodIds() {
        return this.inputGoodIds;
    }

    public FloatList getInputGoodValues() {
        return this.inputGoodValues;
    }

    public IntList getInputGoodStart() {
        return this.inputGoodStart;
    }

    public IntList getInputGoodCount() {
        return this.inputGoodCount;
    }

    public IntList getOutputGoodIds() {
        return this.outputGoodIds;
    }

    public FloatList getOutputGoodValues() {
        return this.outputGoodValues;
    }

    public IntList getOutputGoodStart() {
        return this.outputGoodStart;
    }

    public IntList getOutputGoodCount() {
        return this.outputGoodCount;
    }

    public int getBaseTypeId(int economyBuildingId) {
        return this.baseTypeIds.get(economyBuildingId);
    }

    public int getArtisansTypeId(int economyBuildingId) {
        return this.artisansTypeIds.get(economyBuildingId);
    }

    public int getMaxLevel(int economyBuildingId) {
        return this.maxLevels.get(economyBuildingId);
    }

    @Override
    public String toString() {
        return "EconomyBuildingStore{" +
            "names=" + getNames() +
            ", times=" + getTimes() +
            ", types=" + getTypes() +
            ", onMapFlags=" + getOnMapFlags() +
            ", baseTypeIds=" + this.baseTypeIds +
            ", artisansTypeIds=" + this.artisansTypeIds +
            ", maxLevels=" + this.maxLevels +
            ", inputGoodIds=" + this.inputGoodIds +
            ", inputGoodValues=" + this.inputGoodValues +
            ", inputGoodStart=" + this.inputGoodStart +
            ", inputGoodCount=" + this.inputGoodCount +
            ", outputGoodIds=" + this.outputGoodIds +
            ", outputGoodValues=" + this.outputGoodValues +
            ", outputGoodStart=" + this.outputGoodStart +
            ", outputGoodCount=" + this.outputGoodCount +
            ", goodsCostGoodIds=" + getGoodsCostGoodIds() +
            ", goodsCostValues=" + getGoodsCostValues() +
            ", goodsCostStart=" + getGoodsCostStart() +
            ", goodsCostCount=" + getGoodsCostCount() +
            '}';
    }
}

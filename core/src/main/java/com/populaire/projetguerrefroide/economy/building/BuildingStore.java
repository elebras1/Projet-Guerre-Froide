package com.populaire.projetguerrefroide.economy.building;

import com.github.tommyettinger.ds.*;
import java.util.List;

public class BuildingStore {
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
    private final LongList baseTypeIds;
    private final LongList artisansTypeIds;
    private final LongList inputGoodIds;
    private final FloatList inputGoodValues;
    private final IntList inputGoodStarts;
    private final IntList inputGoodCounts;
    private final LongList outputGoodIds;
    private final FloatList outputGoodValues;
    private final IntList costs;
    private final BooleanList onMap;

    public BuildingStore(List<String> names, IntList times, ByteList types, LongList goodsCostGoodIds, FloatList goodsCostValues, IntList goodsCostStarts, IntList goodsCostCounts, ByteList maxLevels, IntList modifierIds, IntList modifierStart, IntList modifierCount, LongList baseTypeIds, LongList artisansTypeIds, LongList inputGoodIds, FloatList inputGoodValues, IntList inputGoodStarts, IntList inputGoodCounts, LongList outputGoodIds, FloatList outputGoodValues, IntList costs, BooleanList onMap) {
        this.names = names;
        this.times = times;
        this.types = types;
        this.goodsCostGoodIds = goodsCostGoodIds;
        this.goodsCostValues = goodsCostValues;
        this.goodsCostStarts = goodsCostStarts;
        this.goodsCostCounts = goodsCostCounts;
        this.maxLevels = maxLevels;
        this.modifierIds = modifierIds;
        this.modifierStart = modifierStart;
        this.modifierCount = modifierCount;
        this.baseTypeIds = baseTypeIds;
        this.artisansTypeIds = artisansTypeIds;
        this.inputGoodIds = inputGoodIds;
        this.inputGoodValues = inputGoodValues;
        this.inputGoodStarts = inputGoodStarts;
        this.inputGoodCounts = inputGoodCounts;
        this.outputGoodIds = outputGoodIds;
        this.outputGoodValues = outputGoodValues;
        this.costs = costs;
        this.onMap = onMap;
    }

    public List<String> getNames() {
        return this.names;
    }

    public IntList getTimes() {
        return this.times;
    }

    public ByteList getTypes() {
        return this.types;
    }

    public LongList getGoodsCostGoodIds() {
        return this.goodsCostGoodIds;
    }

    public FloatList getGoodsCostValues() {
        return this.goodsCostValues;
    }

    public IntList getGoodsCostStarts() {
        return this.goodsCostStarts;
    }

    public IntList getGoodsCostCounts() {
        return this.goodsCostCounts;
    }

    public ByteList getMaxLevels() {
        return this.maxLevels;
    }

    public IntList getModifierIds() {
        return this.modifierIds;
    }

    public IntList getModifierStart() {
        return this.modifierStart;
    }

    public IntList getModifierCount() {
        return this.modifierCount;
    }

    public LongList getBaseTypeIds() {
        return this.baseTypeIds;
    }

    public LongList getArtisansTypeIds() {
        return this.artisansTypeIds;
    }

    public LongList getInputGoodIds() {
        return this.inputGoodIds;
    }

    public FloatList getInputGoodValues() {
        return this.inputGoodValues;
    }

    public IntList getInputGoodStarts() {
        return this.inputGoodStarts;
    }

    public IntList getInputGoodCounts() {
        return this.inputGoodCounts;
    }

    public LongList getOutputGoodIds() {
        return this.outputGoodIds;
    }

    public FloatList getOutputGoodValues() {
        return this.outputGoodValues;
    }

    public IntList getCosts() {
        return this.costs;
    }

    public BooleanList getOnMap() {
        return this.onMap;
    }

    @Override
    public String toString() {
        return "BuildingStore{" +
                "names=" + this.names +
                ", times=" + this.times +
                ", types=" + this.types +
                ", goodsCostGoodIds=" + this.goodsCostGoodIds +
                ", goodsCostValues=" + this.goodsCostValues +
                ", goodsCostStarts=" + this.goodsCostStarts +
                ", goodsCostCounts=" + this.goodsCostCounts +
                ", maxLevels=" + this.maxLevels +
                ", modifierIds=" + this.modifierIds +
                ", modifierStart=" + this.modifierStart +
                ", modifierCount=" + this.modifierCount +
                ", baseTypeIds=" + this.baseTypeIds +
                ", artisansTypeIds=" + this.artisansTypeIds +
                ", inputGoodIds=" + this.inputGoodIds +
                ", inputGoodValues=" + this.inputGoodValues +
                ", inputGoodStarts=" + this.inputGoodStarts +
                ", inputGoodCounts=" + this.inputGoodCounts +
                ", outputGoodIds=" + this.outputGoodIds +
                ", outputGoodValues=" + this.outputGoodValues +
                ", costsDev=" + this.costs +
                ", onMap=" + this.onMap +
                '}';
    }
}

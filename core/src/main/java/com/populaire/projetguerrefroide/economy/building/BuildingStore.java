package com.populaire.projetguerrefroide.economy.building;

import com.github.tommyettinger.ds.*;
import java.util.List;

public class BuildingStore {
    private final List<String> names;
    private final IntList times;
    private final ByteList types;
    private final IntList goodsCostGoodIds;
    private final FloatList goodsCostValues;
    private final IntList goodsCostStarts;
    private final IntList goodsCostCounts;
    private final ByteList maxLevels;
    private final IntList modifierIds;
    private final IntList modifierStart;
    private final IntList modifierCount;
    private final IntList baseTypeIds;
    private final IntList artisansTypeIds;
    private final IntList inputGoodIds;
    private final FloatList inputGoodValues;
    private final IntList inputGoodStarts;
    private final IntList inputGoodCounts;
    private final IntList outputGoodIds;
    private final FloatList outputGoodValues;
    private final IntList outputGoodStarts;
    private final IntList outputGoodCounts;
    private final IntList costs;
    private final BooleanList onMap;

    public BuildingStore(List<String> names, IntList times, ByteList types, IntList goodsCostGoodIds, FloatList goodsCostValues, IntList goodsCostStarts, IntList goodsCostCounts, ByteList maxLevels, IntList modifierIds, IntList modifierStart, IntList modifierCount, IntList baseTypeIds, IntList artisansTypeIds, IntList inputGoodIds, FloatList inputGoodValues, IntList inputGoodStarts, IntList inputGoodCounts, IntList outputGoodIds, FloatList outputGoodValues, IntList outputGoodStarts, IntList outputGoodCounts, IntList costs, BooleanList onMap) {
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
        this.outputGoodStarts = outputGoodStarts;
        this.outputGoodCounts = outputGoodCounts;
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

    public IntList getGoodsCostGoodIds() {
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

    public IntList getBaseTypeIds() {
        return this.baseTypeIds;
    }

    public IntList getArtisansTypeIds() {
        return this.artisansTypeIds;
    }

    public IntList getInputGoodIds() {
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

    public IntList getOutputGoodIds() {
        return this.outputGoodIds;
    }

    public FloatList getOutputGoodValues() {
        return this.outputGoodValues;
    }

    public IntList getOutputGoodStarts() {
        return this.outputGoodStarts;
    }

    public IntList getOutputGoodCounts() {
        return this.outputGoodCounts;
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
                ", outputGoodStarts=" + this.outputGoodStarts +
                ", outputGoodCounts=" + this.outputGoodCounts +
                ", costsDev=" + this.costs +
                ", onMap=" + this.onMap +
                '}';
    }
}

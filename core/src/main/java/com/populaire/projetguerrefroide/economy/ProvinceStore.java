package com.populaire.projetguerrefroide.economy;

import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.IntList;

public class ProvinceStore {
    private final IntList resourceGoodIds;
    private final IntList buildingIds;
    private final IntList buildingValues;
    private final IntList buildingStarts;
    private final IntList buildingCounts;
    private final IntList amountChildren;
    private final IntList amountAdults;
    private final IntList amountSeniors;
    private final IntList populationTypeIds;
    private final FloatList populationTypeValues;
    private final IntList populationTypeStarts;
    private final IntList populationTypeCounts;
    private final IntList cultureIds;
    private final FloatList cultureValues;
    private final IntList cultureStarts;
    private final IntList cultureCounts;
    private final IntList religionIds;
    private final FloatList religionValues;
    private final IntList religionStarts;
    private final IntList religionCounts;
    private final IntList resourceGoodsSize;
    private final FloatList resourceGoodsProduction;

    public ProvinceStore(IntList resourceGoodIds, IntList buildingIds, IntList buildingValues, IntList buildingStarts, IntList buildingCounts, IntList amountChildren, IntList amountAdults, IntList amountSeniors, IntList populationTypeIds, FloatList populationTypeValues, IntList populationTypeStarts, IntList populationTypeCounts, IntList cultureIds, FloatList cultureValues, IntList cultureStarts, IntList cultureCounts, IntList religionIds, FloatList religionValues, IntList religionStarts, IntList religionCounts) {
        this.resourceGoodIds = resourceGoodIds;
        this.buildingIds = buildingIds;
        this.buildingValues = buildingValues;
        this.buildingStarts = buildingStarts;
        this.buildingCounts = buildingCounts;
        this.amountChildren = amountChildren;
        this.amountAdults = amountAdults;
        this.amountSeniors = amountSeniors;
        this.populationTypeIds = populationTypeIds;
        this.populationTypeValues = populationTypeValues;
        this.populationTypeStarts = populationTypeStarts;
        this.populationTypeCounts = populationTypeCounts;
        this.cultureIds = cultureIds;
        this.cultureValues = cultureValues;
        this.cultureStarts = cultureStarts;
        this.cultureCounts = cultureCounts;
        this.religionIds = religionIds;
        this.religionValues = religionValues;
        this.religionStarts = religionStarts;
        this.religionCounts = religionCounts;
        this.resourceGoodsSize = new IntList(resourceGoodIds.size());
        this.resourceGoodsProduction = new FloatList(resourceGoodIds.size());
    }

    public IntList getResourceGoodIds() {
        return this.resourceGoodIds;
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

    public IntList getAmountChildren() {
        return this.amountChildren;
    }

    public IntList getAmountAdults() {
        return this.amountAdults;
    }

    public IntList getAmountSeniors() {
        return this.amountSeniors;
    }

    public IntList getPopulationTypeIds() {
        return this.populationTypeIds;
    }

    public FloatList getPopulationTypeValues() {
        return this.populationTypeValues;
    }

    public IntList getPopulationTypeStarts() {
        return this.populationTypeStarts;
    }

    public IntList getPopulationTypeCounts() {
        return this.populationTypeCounts;
    }

    public IntList getCultureIds() {
        return this.cultureIds;
    }

    public FloatList getCultureValues() {
        return this.cultureValues;
    }

    public IntList getCultureStarts() {
        return this.cultureStarts;
    }

    public IntList getCultureCounts() {
        return this.cultureCounts;
    }

    public IntList getReligionIds() {
        return this.religionIds;
    }

    public FloatList getReligionValues() {
        return this.religionValues;
    }

    public IntList getReligionStarts() {
        return this.religionStarts;
    }

    public IntList getReligionCounts() {
        return this.religionCounts;
    }

    public IntList getResourceGoodsSize() {
        return this.resourceGoodsSize;
    }

    public FloatList getResourceGoodsProduction() {
        return this.resourceGoodsProduction;
    }

    @Override
    public String toString() {
        return "ProvinceStore{" +
                "resourceGoodIds=" + this.resourceGoodIds +
                ", buildingIds=" + this.buildingIds +
                ", buildingValues=" + this.buildingValues +
                ", buildingStarts=" + this.buildingStarts +
                ", buildingCounts=" + this.buildingCounts +
                ", amountChildren=" + this.amountChildren +
                ", amountAdults=" + this.amountAdults +
                ", amountSeniors=" + this.amountSeniors +
                ", populationTypeIds=" + this.populationTypeIds +
                ", populationTypeValues=" + this.populationTypeValues +
                ", populationTypeStarts=" + this.populationTypeStarts +
                ", populationTypeCounts=" + this.populationTypeCounts +
                ", cultureIds=" + this.cultureIds +
                ", cultureValues=" + this.cultureValues +
                ", cultureStarts=" + this.cultureStarts +
                ", cultureCounts=" + this.cultureCounts +
                ", religionIds=" + this.religionIds +
                ", religionValues=" + this.religionValues +
                ", religionStarts=" + this.religionStarts +
                ", religionCounts=" + this.religionCounts +
                ", resourceGoodsSize=" + this.resourceGoodsSize +
                ", resourceGoodsProduction=" + this.resourceGoodsProduction +
                '}';
    }
}

package com.populaire.projetguerrefroide.map;

import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.IntIntMap;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.LongList;

public class ProvinceStore {
    private final IntIntMap indexById;
    private final IntList ids;
    private final IntList colors;
    private final IntList resourceGoodIds;
    private final IntList buildingIds;
    private final IntList buildingValues;
    private final IntList buildingStarts;
    private final IntList buildingCounts;
    private final IntList amountChildren;
    private final IntList amountAdults;
    private final IntList amountSeniors;
    private final IntList populationTypeIds;
    private final IntList populationTypeValues;
    private final IntList populationTypeStarts;
    private final IntList populationTypeCounts;
    private final LongList cultureIds;
    private final IntList cultureValues;
    private final IntList cultureStarts;
    private final IntList cultureCounts;
    private final IntList religionIds;
    private final IntList religionValues;
    private final IntList religionStarts;
    private final IntList religionCounts;
    private final IntList resourceGoodsSize;
    private final FloatList resourceGoodsProduction;
    private final IntList resourceGoodsPopulationAmountValues;
    private final IntList buildingsPopulationAmountValues;

    public ProvinceStore(IntIntMap indexById, IntList ids, IntList colors, IntList resourceGoodIds, IntList buildingIds, IntList buildingValues, IntList buildingStarts, IntList buildingCounts, IntList amountChildren, IntList amountAdults, IntList amountSeniors, IntList populationTypeIds, IntList populationTypeValues, IntList populationTypeStarts, IntList populationTypeCounts, LongList cultureIds, IntList cultureValues, IntList cultureStarts, IntList cultureCounts, IntList religionIds, IntList religionValues, IntList religionStarts, IntList religionCounts) {
        this.indexById = indexById;
        this.ids = ids;
        this.colors = colors;
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
        this.resourceGoodsSize.setSize(resourceGoodIds.size());
        this.resourceGoodsProduction = new FloatList(resourceGoodIds.size());
        this.resourceGoodsProduction.setSize(resourceGoodIds.size());
        this.resourceGoodsPopulationAmountValues = new IntList(populationTypeIds.size());
        this.resourceGoodsPopulationAmountValues.setSize(populationTypeIds.size());
        this.buildingsPopulationAmountValues = new IntList(populationTypeIds.size());
        this.buildingsPopulationAmountValues.setSize(populationTypeIds.size());
    }

    public IntIntMap getIndexById() {
        return this.indexById;
    }

    public IntList getIds() {
        return this.ids;
    }

    public IntList getColors() {
        return this.colors;
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

    public IntList getPopulationTypeValues() {
        return this.populationTypeValues;
    }

    public IntList getPopulationTypeStarts() {
        return this.populationTypeStarts;
    }

    public IntList getPopulationTypeCounts() {
        return this.populationTypeCounts;
    }

    public LongList getCultureIds() {
        return this.cultureIds;
    }

    public IntList getCultureValues() {
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

    public IntList getReligionValues() {
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

    public IntList getResourceGoodsPopulationAmountValues() {
        return this.resourceGoodsPopulationAmountValues;
    }

    public IntList getBuildingsPopulationAmountValues() {
        return this.buildingsPopulationAmountValues;
    }

    public int getPopulationAmount(int provinceId) {
        int children = this.amountChildren.get(provinceId);
        int adults = this.amountAdults.get(provinceId);
        int seniors = this.amountSeniors.get(provinceId);
        return children + adults + seniors;
    }

    @Override
    public String toString() {
        return "ProvinceStore{" +
                "indexById=" + this.indexById +
                "ids=" + this.ids +
                "colors=" + this.colors +
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

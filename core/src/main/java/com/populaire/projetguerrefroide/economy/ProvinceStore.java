package com.populaire.projetguerrefroide.economy;

import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.IntList;

public class ProvinceStore {
    private final IntList resourceGoodIds;
    private final IntList buildingIds;
    private final IntList buildingsValues;
    private final IntList buildingsStart;
    private final IntList buildingsCount;
    private final IntList amountChildren;
    private final IntList amountAdults;
    private final IntList amountSeniors;
    private final IntList populationTypeIds;
    private final FloatList populationTypeValues;
    private final IntList populationTypeStart;
    private final IntList populationTypeCount;
    private final IntList cultureIds;
    private final FloatList cultureValues;
    private final IntList cultureStart;
    private final IntList cultureCount;
    private final IntList religionIds;
    private final FloatList religionValues;
    private final IntList religionStart;
    private final IntList religionCount;
    private final IntList resourceGoodsSize;
    private final FloatList resourceGoodsProduction;

    public ProvinceStore(IntList resourceGoodIds, IntList buildingIds, IntList buildingsValues, IntList buildingsStart, IntList buildingsCount, IntList amountChildren, IntList amountAdults, IntList amountSeniors, IntList populationTypeIds, FloatList populationTypeValues, IntList populationTypeStart, IntList populationTypeCount, IntList cultureIds, FloatList cultureValues, IntList cultureStart, IntList cultureCount, IntList religionIds, FloatList religionValues, IntList religionStart, IntList religionCount) {
        this.resourceGoodIds = resourceGoodIds;
        this.buildingIds = buildingIds;
        this.buildingsValues = buildingsValues;
        this.buildingsStart = buildingsStart;
        this.buildingsCount = buildingsCount;
        this.amountChildren = amountChildren;
        this.amountAdults = amountAdults;
        this.amountSeniors = amountSeniors;
        this.populationTypeIds = populationTypeIds;
        this.populationTypeValues = populationTypeValues;
        this.populationTypeStart = populationTypeStart;
        this.populationTypeCount = populationTypeCount;
        this.cultureIds = cultureIds;
        this.cultureValues = cultureValues;
        this.cultureStart = cultureStart;
        this.cultureCount = cultureCount;
        this.religionIds = religionIds;
        this.religionValues = religionValues;
        this.religionStart = religionStart;
        this.religionCount = religionCount;
        this.resourceGoodsSize = new IntList(resourceGoodIds.size());
        this.resourceGoodsProduction = new FloatList(resourceGoodIds.size());
    }

    public IntList getResourceGoodIds() {
        return this.resourceGoodIds;
    }

    public IntList getBuildingIds() {
        return this.buildingIds;
    }

    public IntList getBuildingsValues() {
        return this.buildingsValues;
    }

    public IntList getBuildingsStart() {
        return this.buildingsStart;
    }

    public IntList getBuildingsCount() {
        return this.buildingsCount;
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

    public IntList getPopulationTypeStart() {
        return this.populationTypeStart;
    }

    public IntList getPopulationTypeCount() {
        return this.populationTypeCount;
    }

    public IntList getCultureIds() {
        return this.cultureIds;
    }

    public FloatList getCultureValues() {
        return this.cultureValues;
    }

    public IntList getCultureStart() {
        return this.cultureStart;
    }

    public IntList getCultureCount() {
        return this.cultureCount;
    }

    public IntList getReligionIds() {
        return this.religionIds;
    }

    public FloatList getReligionValues() {
        return this.religionValues;
    }

    public IntList getReligionStart() {
        return this.religionStart;
    }

    public IntList getReligionCount() {
        return this.religionCount;
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
                ", buildingsValues=" + this.buildingsValues +
                ", buildingsStart=" + this.buildingsStart +
                ", buildingsCount=" + this.buildingsCount +
                ", amountChildren=" + this.amountChildren +
                ", amountAdults=" + this.amountAdults +
                ", amountSeniors=" + this.amountSeniors +
                ", populationTypeIds=" + this.populationTypeIds +
                ", populationTypeValues=" + this.populationTypeValues +
                ", populationTypeStart=" + this.populationTypeStart +
                ", populationTypeCount=" + this.populationTypeCount +
                ", cultureIds=" + this.cultureIds +
                ", cultureValues=" + this.cultureValues +
                ", cultureStart=" + this.cultureStart +
                ", cultureCount=" + this.cultureCount +
                ", religionIds=" + this.religionIds +
                ", religionValues=" + this.religionValues +
                ", religionStart=" + this.religionStart +
                ", religionCount=" + this.religionCount +
                ", resourceGoodsSize=" + this.resourceGoodsSize +
                ", resourceGoodsProduction=" + this.resourceGoodsProduction +
                '}';
    }
}

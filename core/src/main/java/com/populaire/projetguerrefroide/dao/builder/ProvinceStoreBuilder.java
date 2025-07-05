package com.populaire.projetguerrefroide.dao.builder;

import com.github.tommyettinger.ds.IntList;
import com.populaire.projetguerrefroide.map.ProvinceStore;

public class ProvinceStoreBuilder {
    private final int defaultCapacity;
    private int index;
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
    private final IntList cultureIds;
    private final IntList cultureValues;
    private final IntList cultureStarts;
    private final IntList cultureCounts;
    private final IntList religionIds;
    private final IntList religionValues;
    private final IntList religionStarts;
    private final IntList religionCounts;

    public ProvinceStoreBuilder() {
        this.defaultCapacity = 14797;
        this.index = 0;
        this.ids = new IntList(this.defaultCapacity);
        this.colors = new IntList(this.defaultCapacity);
        this.resourceGoodIds = new IntList();
        this.buildingIds = new IntList();
        this.buildingValues = new IntList();
        this.buildingStarts = new IntList(this.defaultCapacity);
        this.buildingCounts = new IntList(this.defaultCapacity);
        this.amountChildren = new IntList(this.defaultCapacity);
        this.amountAdults = new IntList(this.defaultCapacity);
        this.amountSeniors = new IntList(this.defaultCapacity);
        this.populationTypeIds = new IntList();
        this.populationTypeValues = new IntList();
        this.populationTypeStarts = new IntList(this.defaultCapacity);
        this.populationTypeCounts = new IntList(this.defaultCapacity);
        this.cultureIds = new IntList();
        this.cultureValues = new IntList();
        this.cultureStarts = new IntList(this.defaultCapacity);
        this.cultureCounts = new IntList(this.defaultCapacity);
        this.religionIds = new IntList();
        this.religionValues = new IntList();
        this.religionStarts = new IntList(this.defaultCapacity);
        this.religionCounts = new IntList(this.defaultCapacity);
    }

    private int getDefaultCapacity() {
        return this.defaultCapacity;
    }

    private int getIndex() {
        return this.index;
    }

    public ProvinceStoreBuilder addProvince(int id) {
        this.ids.add(id);
        this.colors.add(-1);

        this.buildingStarts.add(this.buildingIds.size());
        this.buildingCounts.add(0);

        this.populationTypeStarts.add(this.populationTypeIds.size());
        this.populationTypeCounts.add(0);

        this.cultureStarts.add(this.cultureIds.size());
        this.cultureCounts.add(0);

        this.religionStarts.add(this.religionIds.size());
        this.religionCounts.add(0);

        this.index = this.ids.size() - 1;
        return this;
    }

    public ProvinceStoreBuilder addAmountPopulation(int amountChildren, int amountAdults, int amountSeniors) {
        this.amountChildren.add(amountChildren);
        this.amountAdults.add(amountAdults);
        this.amountSeniors.add(amountSeniors);
        return this;
    }

    public ProvinceStoreBuilder addResourceGood(int resourceGoodId) {
        this.resourceGoodIds.add(resourceGoodId);
        return this;
    }

    public ProvinceStoreBuilder addBuilding(int buildingId, int value) {
        this.buildingIds.add(buildingId);
        this.buildingValues.add(value);

        int currentCount = this.buildingCounts.get(this.index);
        this.buildingCounts.set(this.index, currentCount + 1);
        return this;
    }

    public ProvinceStoreBuilder addPopulationType(int populationTypeId, int value) {
        this.populationTypeIds.add(populationTypeId);
        this.populationTypeValues.add(value);

        int currentCount = this.populationTypeCounts.get(this.index);
        this.populationTypeCounts.set(this.index, currentCount + 1);
        return this;
    }

    public ProvinceStoreBuilder addCulture(int cultureId, int value) {
        this.cultureIds.add(cultureId);
        this.cultureValues.add(value);

        int currentCount = this.cultureCounts.get(this.index);
        this.cultureCounts.set(this.index, currentCount + 1);
        return this;
    }

    public ProvinceStoreBuilder addReligion(int religionId, int value) {
        this.religionIds.add(religionId);
        this.religionValues.add(value);

        int currentCount = this.religionCounts.get(this.index);
        this.religionCounts.set(this.index, currentCount + 1);
        return this;
    }

    public ProvinceStore build() {
        return new ProvinceStore(this.ids, this.colors, this.resourceGoodIds, this.buildingIds, this.buildingValues, this.buildingStarts, this.buildingCounts, this.amountChildren, this.amountAdults, this.amountSeniors, this.populationTypeIds, this.populationTypeValues, this.populationTypeStarts, this.populationTypeCounts, this.cultureIds, this.cultureValues, this.cultureStarts, this.cultureCounts, this.religionIds, this.religionValues, this.religionStarts, this.religionCounts);
    }

}

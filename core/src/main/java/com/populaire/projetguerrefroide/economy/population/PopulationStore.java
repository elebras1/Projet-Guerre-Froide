package com.populaire.projetguerrefroide.economy.population;

import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.FloatList;

public class PopulationStore {
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

    public PopulationStore(IntList amountChildren, IntList amountAdults, IntList amountSeniors, IntList populationTypeIds, FloatList populationTypeValues, IntList populationTypeStart, IntList populationTypeCount, IntList cultureIds, FloatList cultureValues, IntList cultureStart, IntList cultureCount, IntList religionIds, FloatList religionValues, IntList religionStart, IntList religionCount) {
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

    @Override
    public String toString() {
        return "Population{" +
            "amountChildren=" + this.amountChildren +
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
            '}';
    }
}

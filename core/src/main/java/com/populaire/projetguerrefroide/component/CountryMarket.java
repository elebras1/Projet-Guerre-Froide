package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.GOOD_COUNT;
import static com.populaire.projetguerrefroide.util.Constants.POP_TYPE_COUNT;

@Component
public record CountryMarket(
    @FixedArray(length = GOOD_COUNT) float[] effectiveGoodPrices,
    @FixedArray(length = GOOD_COUNT) float[] realDemand,
    @FixedArray(length = GOOD_COUNT) float[] demandSatisfaction,
    @FixedArray(length = GOOD_COUNT) float[] domesticMarketPool,
    @FixedArray(length = GOOD_COUNT) float[] stockpiles,
    @FixedArray(length = POP_TYPE_COUNT) float[] lifeCostsByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] everydayCostsByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] luxuryCostsByPopType,
    float spendingRatio,
    float privateInvestmentAmount) {
}

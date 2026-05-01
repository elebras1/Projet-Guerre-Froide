package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.GOOD_COUNT;
import static com.populaire.projetguerrefroide.util.Constants.POP_TYPE_COUNT;

@Component
public record CountryMarket(
    @FixedArray(length = GOOD_COUNT) float[] goodPrices,
    @FixedArray(length = GOOD_COUNT) float[] goodDemandAmounts,
    @FixedArray(length = GOOD_COUNT) float[] goodDemandSatisfactionRatios,
    @FixedArray(length = GOOD_COUNT) float[] goodAmountsPool,
    @FixedArray(length = GOOD_COUNT) float[] goodStockpiles,
    @FixedArray(length = GOOD_COUNT) float[] goodStockpileTargets,
    @FixedArray(length = GOOD_COUNT) float[] goodStockpileDailyDeficits,
    @FixedArray(length = GOOD_COUNT) boolean[] goodDrawingOnStockpiles,
    @FixedArray(length = POP_TYPE_COUNT) float[] lifeCostsByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] everydayCostsByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] luxuryCostsByPopType,
    float treasury,
    float spendingRatio,
    float privateInvestmentAmount) {
}

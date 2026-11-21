package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.simulation.Constants.GOOD_COUNT;

@Component
public record GlobalMarket(
    @FixedArray(length = GOOD_COUNT) float[] goodStockAmounts,
    @FixedArray(length = GOOD_COUNT) float[] goodPrices,
    @FixedArray(length = GOOD_COUNT) float[] goodProductionAmounts,
    @FixedArray(length = GOOD_COUNT) float[] goodDemandAmounts,
    @FixedArray(length = GOOD_COUNT) double[] goodTradeWeights,
    @FixedArray(length = GOOD_COUNT) double[] goodTradeWeightedPrices,
    @FixedArray(length = GOOD_COUNT) double[] goodForeignDemands,
    @FixedArray(length = GOOD_COUNT) double[] goodDeliveredWeights,
    @FixedArray(length = GOOD_COUNT) double[] goodDeliveredValues,
    @FixedArray(length = GOOD_COUNT) boolean[] goodAvailabilities,
    @FixedArray(length = GOOD_COUNT) float[] goodPendingRgoOutputs,
    @FixedArray(length = GOOD_COUNT) float[] goodTrendDemands){
}

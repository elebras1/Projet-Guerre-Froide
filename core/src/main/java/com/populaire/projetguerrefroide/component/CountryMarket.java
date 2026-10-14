package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.GOOD_COUNT;
import static com.populaire.projetguerrefroide.util.Constants.POP_TYPE_COUNT;

@Component
public record CountryMarket(
    @FixedArray(length = GOOD_COUNT) float[] goodPrices,
    @FixedArray(length = GOOD_COUNT) float[] goodDemandAmounts,
    @FixedArray(length = GOOD_COUNT) double[] goodPopulationDemandAmounts,
    @FixedArray(length = GOOD_COUNT) float[] goodDemandSatisfactionRatios,
    @FixedArray(length = GOOD_COUNT) float[] goodStocks,
    @FixedArray(length = POP_TYPE_COUNT) float[] lifeCostsByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] everydayCostsByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] luxuryCostsByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] paidLifeCostsByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] paidEverydayCostsByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] paidLuxuryCostsByPopType,
    @FixedArray(length = GOOD_COUNT) float[] goodProducedAmounts,
    @FixedArray(length = GOOD_COUNT) float[] goodConsumedAmounts,
    @FixedArray(length = GOOD_COUNT) float[] goodImportNeeds,
    @FixedArray(length = GOOD_COUNT) float[] goodExportOffers,
    @FixedArray(length = GOOD_COUNT) float[] goodImportedAmounts,
    @FixedArray(length = GOOD_COUNT) float[] goodExportedAmounts,
    @FixedArray(length = GOOD_COUNT) float[] goodLostAmounts,
    double treasury,
    float privateInvestmentAmount,
    @FixedArray(length = GOOD_COUNT) double[] goodRevenues,
    @FixedArray(length = GOOD_COUNT) double[] goodPendingRevenues,
    float productionValue,
    float tariffRevenue) {
}

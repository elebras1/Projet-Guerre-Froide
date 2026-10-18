package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.MAX_GOODS;

@Component
public record EconomyBuilding(
    long ownerTagId,
    float production,
    float utilization,
    float unitRevenue,
    float unitMargin,
    float profit,
    float profitTrend,
    float dividend,
    double treasury,
    int primaryWorkerAmount,
    int secondaryWorkerAmount,
    float primaryWorkerMinWage,
    float secondaryWorkerMinWage,
    @FixedArray(length = MAX_GOODS) float[] goodInputDemandAmounts,
    @FixedArray(length = MAX_GOODS) float[] inputStocks,
    @FixedArray(length = MAX_GOODS) int[] activeInputGoodIndexes,
    @FixedArray(length = MAX_GOODS) float[] activeInputAmounts,
    boolean advancedRecipe) {

}

package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.MAX_GOODS;

@Component
public record ConstructionSite(
    long buildingId,
    long buildingTypeId,
    long regionInstanceId,
    long countryId,
    long ownerTagId,
    int daysLeft,
    double committedCost,
    @FixedArray(length = MAX_GOODS) int[] goodIndexes,
    @FixedArray(length = MAX_GOODS) float[] goodsLeft,
    @FixedArray(length = MAX_GOODS) float[] dailyGoods,
    @FixedArray(length = MAX_GOODS) float[] demandedGoods) {
}

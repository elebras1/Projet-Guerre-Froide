package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.GOOD_COUNT;
import static com.populaire.projetguerrefroide.util.Constants.MAX_ARTISAN_CRAFTS;

@Component
public record RegionArtisans(
    @FixedArray(length = MAX_ARTISAN_CRAFTS) float[] shares,
    @FixedArray(length = MAX_ARTISAN_CRAFTS) float[] levels,
    @FixedArray(length = MAX_ARTISAN_CRAFTS) float[] production,
    @FixedArray(length = GOOD_COUNT) float[] demandedInputs,
    @FixedArray(length = GOOD_COUNT) float[] inputFills,
    float activeArtisans,
    float inputBudgetRatio,
    double treasury,
    double revenueToday,
    double inputCostToday,
    double plannedInputCost,
    double wagesToday) {
}

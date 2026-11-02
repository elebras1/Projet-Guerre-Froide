package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.GOOD_COUNT;

@Component
public record CountryStrategicStock(
    @FixedArray(length = GOOD_COUNT) float[] stocks,
    @FixedArray(length = GOOD_COUNT) float[] demandedAmounts,
    @FixedArray(length = GOOD_COUNT) float[] purchasedAmounts,
    @FixedArray(length = GOOD_COUNT) float[] plannedReleases,
    @FixedArray(length = GOOD_COUNT) float[] releasedAmounts,
    @FixedArray(length = GOOD_COUNT) float[] lostAmounts,
    double capacity,
    double spentToday,
    double earnedToday,
    float levels) {
}

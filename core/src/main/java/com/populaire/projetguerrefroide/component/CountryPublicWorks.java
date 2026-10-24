package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.GOOD_COUNT;

@Component
public record CountryPublicWorks(
    long targetBuildingId,
    long targetBuildingTypeId,
    float progress,
    float targetReturn,
    @FixedArray(length = GOOD_COUNT) float[] demandedAmounts,
    double spentToday) {
}

package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.OWNER_COUNT;

@Component
public record CountryInvestment(
    @FixedArray(length = OWNER_COUNT) double[] funds,
    @FixedArray(length = OWNER_COUNT) double[] committed,
    @FixedArray(length = OWNER_COUNT) double[] plannedToday,
    @FixedArray(length = OWNER_COUNT) double[] spentToday,
    int sitesInProgress,
    int sitesStarted,
    int sitesCompleted) {
}

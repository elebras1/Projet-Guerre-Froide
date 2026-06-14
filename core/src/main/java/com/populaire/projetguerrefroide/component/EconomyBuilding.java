package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.MAX_GOODS;

@Component
public record EconomyBuilding(
    long ownerTagId,
    float production,
    float scale,
    float profit,
    int primaryWorkerAmount,
    int secondaryWorkerAmount,
    float primaryWorkerMinWage,
    float secondaryWorkerMinWage,
    @FixedArray(length = MAX_GOODS) float[] goodInputDemandAmounts) {

}

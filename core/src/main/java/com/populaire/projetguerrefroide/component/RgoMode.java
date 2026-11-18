package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.simulation.Constants.GOOD_COUNT;
import static com.populaire.projetguerrefroide.simulation.Constants.MAX_RGO_MODE_INPUTS;

@Component
public record RgoMode(
    int index,
    long technologyId,
    @FixedArray(length = GOOD_COUNT) boolean[] goods,
    @FixedArray(length = MAX_RGO_MODE_INPUTS) int[] inputGoodIndexes,
    @FixedArray(length = MAX_RGO_MODE_INPUTS) float[] inputAmounts,
    float output) {
}

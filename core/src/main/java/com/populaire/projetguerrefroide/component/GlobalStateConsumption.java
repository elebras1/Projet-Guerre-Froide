package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.MAX_GOODS;

@Component
public record GlobalStateConsumption(
    @FixedArray(length = MAX_GOODS) int[] administrationGoodIndexes,
    @FixedArray(length = MAX_GOODS) float[] administrationGoodAmounts,
    @FixedArray(length = MAX_GOODS) int[] publicHealthGoodIndexes,
    @FixedArray(length = MAX_GOODS) float[] publicHealthGoodAmounts) {
}

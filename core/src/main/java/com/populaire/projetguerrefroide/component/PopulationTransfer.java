package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.POP_TYPE_COUNT;

@Component
public record PopulationTransfer(
    @FixedArray(length = POP_TYPE_COUNT) int[] arrivingAmounts,
    @FixedArray(length = POP_TYPE_COUNT) float[] arrivingSavings,
    @FixedArray(length = POP_TYPE_COUNT) float[] arrivingDeposits) {
}

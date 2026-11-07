package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.simulation.Constants.MAX_MODIFIERS;

@Component
public record Modifiers(
    @FixedArray(length = MAX_MODIFIERS) float[] values,
    @FixedArray(length = MAX_MODIFIERS) long[] tagIds
) {
}

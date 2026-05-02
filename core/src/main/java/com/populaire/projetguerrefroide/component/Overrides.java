package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.MAX_OVERRIDES;

@Component
public record Overrides(
    @FixedArray(length = MAX_OVERRIDES) float[] values,
    @FixedArray(length = MAX_OVERRIDES) long[] tagIds
) {
}

package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.MAX_MODIFIERS;

@Component
public record Modifiers(@FixedArray(length = MAX_MODIFIERS) float[] values, @FixedArray(length = MAX_MODIFIERS) long[] tagIds) {
}

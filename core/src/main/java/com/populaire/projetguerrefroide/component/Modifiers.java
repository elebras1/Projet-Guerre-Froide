package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

@Component
public record Modifiers(@FixedArray(length = 8) float[] values, @FixedArray(length = 8) long[] tagIds) {
}

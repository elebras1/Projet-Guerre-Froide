package com.populaire.projetguerrefroide;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

@Component
public record Demographics(
    @FixedArray(length = 32) float cultureRatios,
    @FixedArray(length = 10) float religionRatios) {
}

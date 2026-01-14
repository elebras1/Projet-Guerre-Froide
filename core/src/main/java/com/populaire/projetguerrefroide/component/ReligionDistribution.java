package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

@Component
public record ReligionDistribution(@FixedArray(length = 6) long[] populationIds, @FixedArray(length = 6) int[] populationAmounts) {
}

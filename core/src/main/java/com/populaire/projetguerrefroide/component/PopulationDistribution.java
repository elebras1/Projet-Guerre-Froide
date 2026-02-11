package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

@Component
public record PopulationDistribution(@FixedArray(length = 12) int[] amounts) {
}

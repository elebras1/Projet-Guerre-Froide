package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

@Component
public record SpecialBuilding(int time, int cost, @FixedArray(length = 8) long[] goodCostIds, @FixedArray(length = 8) float[] goodCostValues) {
}

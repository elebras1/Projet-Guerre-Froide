package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.MAX_GOODS;

@Component
public record SpecialBuildingType(int time, int cost, @FixedArray(length = MAX_GOODS) long[] goodCostIds, @FixedArray(length = MAX_GOODS) float[] goodCostAmounts) {
}

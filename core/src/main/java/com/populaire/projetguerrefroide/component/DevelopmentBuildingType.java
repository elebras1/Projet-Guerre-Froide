package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.MAX_GOODS;

@Component
public record DevelopmentBuildingType(int time, @FixedArray(length = MAX_GOODS) int[] goodCostIndexes, @FixedArray(length = MAX_GOODS) long[] goodCostIds, @FixedArray(length = MAX_GOODS) float[] goodCostAmounts, @FixedArray(length = MAX_GOODS) int[] maintenanceGoodIndexes, @FixedArray(length = MAX_GOODS) float[] maintenanceGoodAmounts, int maxLevel) {
}

package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

@Component
public record EconomyBuilding(int time, long productionTypeId, long artisansTypeId, int maxLevel, @FixedArray(length = 8) long[] goodCostIds, @FixedArray(length = 8) float[] goodCostValues, @FixedArray(length = 8) long[] inputGoodIds, @FixedArray(length = 8) float[] inputGoodValues, long outputGoodId, float outputGoodValue) {
}

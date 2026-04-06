package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.MAX_GOODS;
import static com.populaire.projetguerrefroide.util.Constants.MAX_POPS;

@Component
public record EconomyBuildingType(
    int time,
    int maxLevel,
    @FixedArray(length = MAX_GOODS) long[] goodCostIds,
    @FixedArray(length = MAX_GOODS) float[] goodCostAmounts,
    @FixedArray(length = MAX_GOODS) long[] goodInputIds,
    @FixedArray(length = MAX_GOODS) float[] goodInputAmounts,
    long goodOutputId,
    float goodOutputAmount,
    int workforce,
    long ownerId,
    @FixedArray(length = MAX_POPS) long[] workerPopTypeIds,
    @FixedArray(length = MAX_POPS) float[] workerPopTypeRatios,
    @FixedArray(length = MAX_POPS) float[] workerPopTypeEffectMultipliers) {
}

package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.MAX_GOODS;

@Component
public record EconomyBuildingType(
    int time,
    int maxLevel,
    @FixedArray(length = MAX_GOODS) int[] goodCostIndexes,
    @FixedArray(length = MAX_GOODS) long[] goodCostIds,
    @FixedArray(length = MAX_GOODS) float[] goodCostAmounts,
    @FixedArray(length = MAX_GOODS) int[] goodInputIndexes,
    @FixedArray(length = MAX_GOODS) long[] goodInputIds,
    @FixedArray(length = MAX_GOODS) float[] goodInputAmounts,
    long goodOutputId,
    float goodOutputAmount,
    int workforce,
    long ownerId,
    int primaryWorkerPopTypeIndex,
    long primaryWorkerPopTypeId,
    float primaryWorkerPopTypeRatio,
    float primaryWorkerEffectMultiplier,
    int secondaryWorkerPopTypeIndex,
    long secondaryWorkerPopTypeId,
    float secondaryWorkerPopTypeRatio,
    float secondaryWorkerEffectMultiplier
) {}

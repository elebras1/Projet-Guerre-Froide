package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.MAX_POPS;

@Component
public record ResourceGatheringType(
    int workforce,
    long ownerId,
    @FixedArray(length = MAX_POPS) int[] workerPopTypeIndexes,
    @FixedArray(length = MAX_POPS) long[] workerPopTypeIds,
    @FixedArray(length = MAX_POPS) float[] workerPopTypeRatios,
    @FixedArray(length = MAX_POPS) float[] workerPopTypeEffectMultipliers) {
}

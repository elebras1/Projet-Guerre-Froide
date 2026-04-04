package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

@Component
public record ResourceGatheringType(
    int workforce,
    long ownerId,
    @FixedArray(length = 4) long[] workerPopTypeIds,
    @FixedArray(length = 4) float[] workerPopTypeRatios,
    @FixedArray(length = 4) float[] workerPopTypeEffectMultipliers) {
}

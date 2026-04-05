package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

@Component
public record Province(
    @FixedArray(length = 8) long[] coreIds,
    long ownerId,
    long controllerId,
    long terrainId,
    int childrenAmount,
    int adultsAmount,
    int seniorsAmount,
    @FixedArray(length = 20) long[] cultureIds,
    @FixedArray(length = 20) int[] cultureAmounts,
    @FixedArray(length = 20) long[] religionIds,
    @FixedArray(length = 20) int[] religionAmounts) {
}

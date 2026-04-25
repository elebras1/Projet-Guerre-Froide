package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.*;

@Component
public record Province(
    @FixedArray(length = MAX_PROVINCE_CORES) long[] coreIds,
    long ownerId,
    long controllerId,
    long terrainId,
    long regionId,
    long continentId,
    long regionInstanceId,
    int childrenAmount,
    int adultsAmount,
    int seniorsAmount,
    @FixedArray(length = MAX_CULTURES) long[] cultureIds,
    @FixedArray(length = MAX_CULTURES) int[] cultureAmounts,
    @FixedArray(length = MAX_RELIGIONS) long[] religionIds,
    @FixedArray(length = MAX_RELIGIONS) int[] religionAmounts) {
}

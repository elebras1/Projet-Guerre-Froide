package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.*;

@Component
public record PopulationType(
    @FixedArray(length = MAX_LIFE_NEEDS_GOODS) long[] lifeNeedsGoodIds,
    @FixedArray(length = MAX_LIFE_NEEDS_GOODS) float[] lifeNeedsGoodAmounts,
    @FixedArray(length = MAX_EVERYDAY_NEEDS_GOODS) long[] everydayNeedsGoodIds,
    @FixedArray(length = MAX_EVERYDAY_NEEDS_GOODS) float[] everydayNeedsGoodAmounts,
    @FixedArray(length = MAX_LUXURY_DEMAND_GOODS) long[] luxuryNeedsGoodIds,
    @FixedArray(length = MAX_LUXURY_DEMAND_GOODS) float[] luxuryNeedsGoodAmounts,
    int strata) {
}


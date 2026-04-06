package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.MAX_LUXURY_DEMAND_GOODS;
import static com.populaire.projetguerrefroide.util.Constants.MAX_STANDARD_DEMAND_GOODS;

@Component
public record PopulationType(
    @FixedArray(length = MAX_STANDARD_DEMAND_GOODS) long[] standardDemandGoodIds,
    @FixedArray(length = MAX_STANDARD_DEMAND_GOODS) float[] standardDemandGoodValues,
    @FixedArray(length = MAX_LUXURY_DEMAND_GOODS) long[] luxuryDemandGoodIds,
    @FixedArray(length = MAX_LUXURY_DEMAND_GOODS) float[] luxuryDemandGoodValues,
    int strata) {
}


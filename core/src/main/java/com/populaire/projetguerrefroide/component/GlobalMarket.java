package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.GOOD_COUNT;

@Component
public record GlobalMarket(
    @FixedArray(length = GOOD_COUNT) float[] goodAmountsPool,
    @FixedArray(length = GOOD_COUNT) float[] goodPrices,
    @FixedArray(length = GOOD_COUNT) float[] goodProductionAmounts,
    @FixedArray(length = GOOD_COUNT) float[] goodDemandAmounts,
    @FixedArray(length = GOOD_COUNT) float[] goodLeftoverAmounts){
}

package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.MAX_GOODS;

@Component
public record Unit(
    long forceTypeTagId,
    long typeTagId,
    int attack,
    int defence,
    int priority,
    int defaultOrganisation,
    int supplyConsumption,
    int weightedValue,
    int buildTime,
    int maximumSpeed,
    int maxStrength,
    @FixedArray(length = MAX_GOODS) long[] buildCostIds,
    @FixedArray(length = MAX_GOODS) float[] buildCostAmounts,
    @FixedArray(length = MAX_GOODS) long[] supplyCostIds,
    @FixedArray(length = MAX_GOODS) float[] supplyCostAmounts){

}

package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.POP_TYPE_COUNT;

@Component
public record RegionInstanceIncome(
    @FixedArray(length = POP_TYPE_COUNT) float[] minWagesByPopType,
    @FixedArray(length = POP_TYPE_COUNT) int[] workersByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] profitShareByPopType,
    float capitalistProfitShare,
    float aristocratProfitShare,
    float countryProfitShare
) {
}

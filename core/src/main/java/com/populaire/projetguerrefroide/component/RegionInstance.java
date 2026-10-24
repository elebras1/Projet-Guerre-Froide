package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.GOOD_COUNT;
import static com.populaire.projetguerrefroide.util.Constants.POP_TYPE_COUNT;

@Component
public record RegionInstance(
    long regionId,
    long ownerId,
    @FixedArray(length = POP_TYPE_COUNT) float[] workerPopTypeEmploymentRatios,
    @FixedArray(length = POP_TYPE_COUNT) float[] wagePremiumByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] vacanciesByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] applicantsByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] statePostsByPopType,
    float taxCollectionShare,
    float managementOutputBonus,
    float infrastructureBonusSum,
    int provinceCount) {
}

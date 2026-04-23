package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.*;

@Component
public record Demographics(
    int totalPopulation,
    int totalEmployment,
    float consciousness,
    float militancy,
    float literacy,
    float savings,
    float lifeNeedsSatisfaction,
    float everydayNeedsSatisfaction,
    float luxuryNeedsSatisfaction,
    @FixedArray(length = POP_TYPE_COUNT) int[] totalByPopType,
    @FixedArray(length = POP_TYPE_COUNT) int[] employmentByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] consciousnessByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] militancyByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] literacyByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] savingsByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] lifeNeedsSatisfactionByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] everydayNeedsSatisfactionByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] luxuryNeedsSatisfactionByPopType,
    int totalChildren,
    int totalAdults,
    int totalSeniors) {
}

package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.*;

@Component
public record Demographics(
    int totalPopulation,
    int totalEmployment,
    float averageConsciousness,
    float averageMilitancy,
    float averageLiteracy,
    float totalSavings,
    @FixedArray(length = POP_TYPE_COUNT) int[] totalByPopType,
    @FixedArray(length = POP_TYPE_COUNT) int[] employmentByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] consciousnessByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] militancyByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] literacyByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] savingsByPopType,
    int totalChildren,
    int totalAdults,
    int totalSeniors) {
}

package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.*;

@Component
public record Demographics(
    long totalPopulation,
    long totalEmployment,
    float averageConsciousness,
    float averageMilitancy,
    float averageLiteracy,
    float totalSavings,
    @FixedArray(length = POP_TYPE_COUNT) long[] amountByPopType,
    @FixedArray(length = POP_TYPE_COUNT) long[] employmentByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] consciousnessByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] militancyByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] literacyByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] savingsByPopType,
    long totalChildren,
    long totalAdults,
    long totalSeniors,
    @FixedArray(length = CULTURE_COUNT) long[] cultureTotals,
    @FixedArray(length = RELIGION_COUNT) long[] religionTotals,
    @FixedArray(length = IDEOLOGY_COUNT) long[] ideologyTotals) {
}

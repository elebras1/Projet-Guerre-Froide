package com.populaire.projetguerrefroide;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

@Component
public record Demographics(
    long totalPopulation,
    long totalEmployment,
    float averageConsciousness,
    float averageMilitancy,
    float averageLiteracy,
    float totalSavings,
    @FixedArray(length = 12) long[] amountByPopType,
    @FixedArray(length = 12) long[] employmentByPopType,
    @FixedArray(length = 12) float[] consciousnessByPopType,
    @FixedArray(length = 12) float[] militancyByPopType,
    @FixedArray(length = 12) float[] literacyByPopType,
    @FixedArray(length = 12) float[] savingsByPopType,
    long totalChildren,
    long totalAdults,
    long totalSeniors,
    @FixedArray(length = 20) long[] cultureIds,
    @FixedArray(length = 20) long[] cultureTotals,
    @FixedArray(length = 20) long[] religionIds,
    @FixedArray(length = 20) long[] religionTotals) {
}

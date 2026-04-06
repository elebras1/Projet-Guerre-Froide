package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.*;
import static com.populaire.projetguerrefroide.util.Constants.IDEOLOGY_COUNT;

@Component
public record CountryDemographics(
    long totalPopulation,
    long totalEmployment,
    float averageConsciousness,
    float averageMilitancy,
    float averageLiteracy,
    float totalSavings,
    @FixedArray(length = POP_TYPE_COUNT) long[] totalByPopType,
    @FixedArray(length = POP_TYPE_COUNT) long[] employmentByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] consciousnessByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] militancyByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] literacyByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] savingsByPopType,
    long totalChildren,
    long totalAdults,
    long totalSeniors,
    @FixedArray(length = CULTURE_COUNT) long[] totalByCulture,
    @FixedArray(length = RELIGION_COUNT) long[] totalByReligion,
    @FixedArray(length = IDEOLOGY_COUNT) long[] totalByIdeology) {
}

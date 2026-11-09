package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.simulation.Constants.MAX_TECHNOLOGIES;
import static com.populaire.projetguerrefroide.simulation.Constants.TECHNOLOGY_TREE_COUNT;

@Component
public record CountryTechnology(
    @FixedArray(length = MAX_TECHNOLOGIES) boolean[] acquired,
    @FixedArray(length = TECHNOLOGY_TREE_COUNT) int[] currentTechnologyIndexes,
    @FixedArray(length = MAX_TECHNOLOGIES) float[] progress) {
}

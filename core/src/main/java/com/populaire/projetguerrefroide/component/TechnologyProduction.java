package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.simulation.Constants.MAX_TECHNOLOGY_EFFECTS;

@Component
public record TechnologyProduction(
    @FixedArray(length = MAX_TECHNOLOGY_EFFECTS) int[] rgoGoodIndexes,
    @FixedArray(length = MAX_TECHNOLOGY_EFFECTS) float[] rgoProductivities,
    @FixedArray(length = MAX_TECHNOLOGY_EFFECTS) int[] factoryTypeIndexes,
    @FixedArray(length = MAX_TECHNOLOGY_EFFECTS) float[] factoryProductivities,
    @FixedArray(length = MAX_TECHNOLOGY_EFFECTS) int[] recipeTypeIndexes) {
}

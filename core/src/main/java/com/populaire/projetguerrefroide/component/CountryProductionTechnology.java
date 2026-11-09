package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.simulation.Constants.GOOD_COUNT;
import static com.populaire.projetguerrefroide.simulation.Constants.MAX_ECONOMY_BUILDING_TYPES;

@Component
public record CountryProductionTechnology(
    @FixedArray(length = GOOD_COUNT) float[] rgoProductivities,
    @FixedArray(length = MAX_ECONOMY_BUILDING_TYPES) float[] factoryProductivities,
    @FixedArray(length = MAX_ECONOMY_BUILDING_TYPES) boolean[] advancedRecipes) {
}

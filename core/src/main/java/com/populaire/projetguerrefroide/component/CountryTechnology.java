package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.simulation.Constants.MAX_TECHNOLOGIES;

@Component
public record CountryTechnology(
    @FixedArray(length = MAX_TECHNOLOGIES) boolean[] acquired,
    int currentTechnologyIndex,
    float progress) {
}

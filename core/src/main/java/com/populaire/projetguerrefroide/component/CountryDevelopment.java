package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;

@Component
public record CountryDevelopment(
    float literacy,
    float outputRatio,
    float outputPosition,
    float index,
    float researchFactor) {
}

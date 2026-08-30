package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;

@Component
public record CountryProductionPolicy(float factoryOutputModifier, float factoryInputModifier, float rgoOutputModifier, float constructionSpeed, float maximumEconomyScaleFactor) {
}


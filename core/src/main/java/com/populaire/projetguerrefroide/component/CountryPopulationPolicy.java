package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;

@Component
public record CountryPopulationPolicy(float popGrowthFactor, float migrationPull, float migrationPush) {
}


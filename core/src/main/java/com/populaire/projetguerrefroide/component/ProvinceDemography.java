package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;

@Component
public record ProvinceDemography(
    float childrenPerAdult,
    float childrenRemainder,
    float seniorsRemainder,
    float adultEntryShare,
    float famineDeathRate) {
}

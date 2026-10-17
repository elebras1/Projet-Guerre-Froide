package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;

@Component
public record CountrySpendingBudget(
    float administrationBudget,
    float militaryBudget,
    float educationBudget,
    float socialBudget,
    float administrationWeight,
    float militaryWeight,
    float educationWeight,
    float socialWeight) {
}

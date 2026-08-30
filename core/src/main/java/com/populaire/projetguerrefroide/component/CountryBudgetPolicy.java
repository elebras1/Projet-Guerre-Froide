package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;

@Component
public record CountryBudgetPolicy(float socialSpendingRate, float militarySpendingRate, float administrationSpendingRate, float popSpending) {
}


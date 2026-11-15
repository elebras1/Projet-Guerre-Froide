package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;

@Component
/**
 * Labour laws of a country. administeredWages (decision 234, economy plan annex 15): the state sets the wages of the
 * buildings it owns at the legal minimum, without the regional scarcity premium.
 */
public record CountryLaborPolicy(float minWageFactor, boolean slaveryAllowed, boolean administeredWages) {
}


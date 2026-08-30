package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;

@Component
public record CountryTaxPolicy(float poorTaxRate, float middleTaxRate, float richTaxRate) {
}


package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;

@Component
public record CountryEconomicPolicy(
    float poorTaxRate,
    float middleTaxRate,
    float richTaxRate,
    float socialSpendingRate,
    float militarySpendingRate,
    float educationSpendingRate,
    float administrationSpendingRate,
    float tariffRate,
    float capitalistProfitShare,
    float workerProfitShare,
    float aristocratProfitShare,
    float stateProfitShare,
    float minWageFactor,
    float educationEfficiency,
    float factoryOutputModifier,
    float factoryInputModifier,
    float rgoOutputModifier,
    float constructionSpeed,
    float popGrowthFactor,
    float migrationPull
) {}

package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;

@Component
public record CountryEffectPolicy(
    float poorTaxRate,
    float middleTaxRate,
    float richTaxRate,
    float socialSpendingRate,
    float militarySpendingRate,
    float educationSpendingRate,
    float administrationSpendingRate,
    float tariffRate,
    float capitalistProfitShareRate,
    float workerProfitShareRate,
    float aristocratProfitShareRate,
    float stateProfitShareRate,
    float minWageFactor,
    float educationEfficiency,
    float factoryOutputModifier,
    float factoryInputModifier,
    float rgoOutputModifier,
    float constructionSpeed,
    float popGrowthFactor,
    float migrationPull,
    float popSpending,
    boolean slaveryAllowed,
    float politicalConsciousness,
    float politicalRadicalism,
    float suppression,
    float socialMobility,
    float classRigidity,
    float administrativeEfficiency,
    float religiousConversionSpeed,
    float secularism,
    float assimilationRate,
    float migrationPush,
    float maximumEconomyScaleFactor) {

}

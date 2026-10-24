package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;

@Component
public record Population(
    long countryId,
    long provinceId,
    long regionInstanceId,
    int amount,
    int employment,
    float consciousness,
    float militancy,
    float literacy,
    float savings,
    float lifeNeedsSatisfaction,
    float everydayNeedsSatisfaction,
    float luxuryNeedsSatisfaction,
    float lifeNeedsDemandRatio,
    float everydayNeedsDemandRatio,
    float luxuryNeedsDemandRatio,
    float income,
    float unspentBudget,
    float deposit) {
}

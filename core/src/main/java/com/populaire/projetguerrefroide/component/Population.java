package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;

@Component
public record Population(
    int index,
    long typeId,
    long countryId,
    long provinceId,
    int amount,
    int employment,
    float consciousness,
    float militancy,
    float literacy,
    float savings,
    float lifeNeedsSatisfaction,
    float everydayNeedsSatisfaction,
    float luxuryNeedsSatisfaction) {
}

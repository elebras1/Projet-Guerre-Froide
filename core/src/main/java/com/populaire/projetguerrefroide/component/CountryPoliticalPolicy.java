package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;

@Component
public record CountryPoliticalPolicy(float politicalConsciousness, float politicalRadicalism, float suppression, float socialMobility, float classRigidity, float administrativeEfficiency) {
}


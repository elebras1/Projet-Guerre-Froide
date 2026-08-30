package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;

@Component
public record CountryLaborPolicy(float minWageFactor, boolean slaveryAllowed) {
}


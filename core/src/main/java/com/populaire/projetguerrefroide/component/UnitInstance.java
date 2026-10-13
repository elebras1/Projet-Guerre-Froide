package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;

@Component
public record UnitInstance(long formationId, long typeId, float strength, float organisation, float experience, String nameId) {
}

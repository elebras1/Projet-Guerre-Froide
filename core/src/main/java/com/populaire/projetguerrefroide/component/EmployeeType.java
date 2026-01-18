package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;

@Component
public record EmployeeType(long populationTypeId, float amount, float effectMultiplier) {
}

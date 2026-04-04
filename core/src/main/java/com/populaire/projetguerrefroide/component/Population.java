package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;

@Component
public record Population(long popTypeId, long amount, long employment, float consciousness, float militancy, float literacy, float savings) {
}

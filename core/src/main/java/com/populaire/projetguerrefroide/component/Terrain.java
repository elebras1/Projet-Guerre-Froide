package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;

@Component
public record Terrain(byte movementCost, byte temperature, byte humidity, byte precipitation, int color) {
}

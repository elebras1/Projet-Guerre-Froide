package com.populaire.projetguerrefroide.entity;

import com.github.elebras1.flecs.annotation.FlecsComponent;

@FlecsComponent
public record Terrain(byte movementCost, byte temperature, byte humidity, byte precipitation, int color) {
}

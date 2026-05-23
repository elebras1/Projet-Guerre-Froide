package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;

@Component
public record ExpansionBuilding(long buildingId, int timeLeft, int levelsQueued) {
}

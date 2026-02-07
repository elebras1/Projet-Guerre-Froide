package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;

@Component
public record ExpansionBuilding(long buildingId, int timeLeft, int levelsQueued) {
}

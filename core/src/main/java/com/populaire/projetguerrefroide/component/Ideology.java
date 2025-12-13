package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.FlecsComponent;

@FlecsComponent
public record Ideology(int color, short factionDriftingSpeed) {
}

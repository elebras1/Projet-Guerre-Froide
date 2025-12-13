package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.FlecsComponent;

@FlecsComponent
public record Minister(String name, String imageFileName, float loyalty, int startDate, int deathDate) {
}

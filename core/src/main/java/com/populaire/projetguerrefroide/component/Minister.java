package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedString;

@Component
public record Minister(String name, @FixedString(size = 64) String imageFileName, float loyalty, int startDate, int deathDate) {
}

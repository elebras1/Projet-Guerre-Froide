package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.simulation.Constants.MAX_TECHNOLOGY_PREREQUISITES;

@Component
public record Technology(
    int index,
    int tree,
    int era,
    int year,
    int time,
    @FixedArray(length = MAX_TECHNOLOGY_PREREQUISITES) long[] prerequisiteIds) {
}

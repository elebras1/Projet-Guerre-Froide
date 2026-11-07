package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.simulation.Constants.MAX_ARTISAN_CRAFTS;

@Component
public record GlobalArtisanCrafts(
    @FixedArray(length = MAX_ARTISAN_CRAFTS) long[] buildingTypeIds,
    int count,
    int artisanPopTypeIndex) {
}

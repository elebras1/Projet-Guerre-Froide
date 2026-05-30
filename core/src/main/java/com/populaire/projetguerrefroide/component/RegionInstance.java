package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.GOOD_COUNT;
import static com.populaire.projetguerrefroide.util.Constants.POP_TYPE_COUNT;

@Component
public record RegionInstance(
    long regionId,
    long ownerId,
    @FixedArray(length = POP_TYPE_COUNT) float[] workerPopTypeEmploymentRatios) {
}

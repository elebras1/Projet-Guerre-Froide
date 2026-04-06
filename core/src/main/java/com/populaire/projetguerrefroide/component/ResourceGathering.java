package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.POP_TYPE_COUNT;

@Component
public record ResourceGathering(
    long rgoTypeId,
    long goodId,
    int goodIndex,
    float goodAmount,
    int size,
    float production,
    @FixedArray(length = POP_TYPE_COUNT) int[] workerEmployments) {

}


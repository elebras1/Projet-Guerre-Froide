package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

@Component
public record ResourceGathering(long rgoTypeId, long goodId, int goodIndex, float goodAmount, int size, float production, @FixedArray(length = 12) int[] workerEmployments) {

}


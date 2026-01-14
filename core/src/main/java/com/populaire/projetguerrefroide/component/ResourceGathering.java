package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

@Component
public record ResourceGathering(long goodId, int size, float production, @FixedArray(length = 6) int[] hiredWorkers) {

}


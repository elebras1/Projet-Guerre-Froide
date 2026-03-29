package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

@Component
public record LocalMarket(int index, long regionId, long ownerId, @FixedArray(length = 40) float[] goodProductions, @FixedArray(length = 40) float[] goodConsumptions) {
}

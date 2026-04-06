package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.GOOD_COUNT;

@Component
public record LocalMarket(long regionId, long ownerId, @FixedArray(length = GOOD_COUNT) float[] goodProductions, @FixedArray(length = GOOD_COUNT) float[] goodConsumptions) {
}

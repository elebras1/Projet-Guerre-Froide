package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.GOOD_COUNT;
import static com.populaire.projetguerrefroide.util.Constants.POP_TYPE_COUNT;

@Component
public record CountryMilitary(
    @FixedArray(length = GOOD_COUNT) float[] supplyNeeds,
    @FixedArray(length = GOOD_COUNT) float[] demandedAmounts,
    double spentToday,
    float supplySatisfaction,
    @FixedArray(length = POP_TYPE_COUNT) float[] posts,
    @FixedArray(length = POP_TYPE_COUNT) float[] filledPosts,
    int units) {
}

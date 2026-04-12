package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.POP_TYPE_COUNT;

@Component
public record EconomyBuilding(
    float production,
    float cashReserves,
    float scale,
    int primaryWorkerAmount,
    int secondaryWorkerAmount) {

}

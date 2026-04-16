package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;

@Component
public record ResourceGathering(
    long typeId,
    long goodId,
    int goodIndex,
    float goodAmount,
    int size,
    float production,
    float scale,
    int workerAmount,
    int slaveAmount) {

}


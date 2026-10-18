package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;

@Component
public record ResourceGathering(
    long goodId,
    int goodIndex,
    float efficiency,
    int size,
    float production,
    float scale,
    int workerAmount,
    int slaveAmount,
    float profit,
    float workerMinWage,
    double treasury) {

}


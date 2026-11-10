package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;

@Component
public record ResourceGathering(
    long goodId,
    int goodIndex,
    float efficiency,
    int size,
    float production,
    float employmentRemainder,
    int workerAmount,
    int slaveAmount,
    int vacancies,
    float applicants,
    float profit,
    float unitMargin,
    float workerMinWage,
    double treasury,
    int mechanization,
    float upkeepValue,
    float upkeepServed,
    float upkeepPartsDemanded,
    float upkeepFuelDemanded,
    float workerOutput) {

}


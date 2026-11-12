package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.simulation.Constants.MAX_RGO_MODE_INPUTS;

@Component
public record ResourceGathering(
    long goodId,
    int goodIndex,
    float efficiency,
    float endowment,
    float endowmentShare,
    float potential,
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
    float workerOutput,
    int mode,
    float modeServed,
    float modeBaseProduction,
    @FixedArray(length = MAX_RGO_MODE_INPUTS) float[] modeInputsDemanded,
    int mechanizationBase,
    float familyShare,
    float monthIncome,
    float monthWorkerDays,
    float familyRatio,
    float workerPay) {

}


package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.POP_TYPE_COUNT;

@Component
public record CountryBudget(
    @FixedArray(length = POP_TYPE_COUNT) float[] wageRatesByPopType,
    @FixedArray(length = POP_TYPE_COUNT) float[] socialRatesByPopType,
    double targetTreasury,
    double reservedNeeds,
    double investableSurplus,
    double revenueTrend,
    float spendingRatio,
    float subsidyAllowance,
    float wagesBudget,
    float socialBudget,
    float stateConsumptionBudget,
    float publicWorksBudget,
    float publicWorksLoan,
    float militarySupplyRatio) {
}

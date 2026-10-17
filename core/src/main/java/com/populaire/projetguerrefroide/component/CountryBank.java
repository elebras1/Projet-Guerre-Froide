package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.POP_TYPE_COUNT;

@Component
public record CountryBank(
    double deposits,
    double stateLoans,
    float loansToday,
    float transfersBudget,
    @FixedArray(length = POP_TYPE_COUNT) float[] transferRatesByPopType) {
}

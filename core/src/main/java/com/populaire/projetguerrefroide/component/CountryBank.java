package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.OWNER_COUNT;

@Component
public record CountryBank(
    double reserves,
    double deposits,
    @FixedArray(length = OWNER_COUNT) double[] loans,
    @FixedArray(length = OWNER_COUNT) double[] debtServiceDue,
    @FixedArray(length = OWNER_COUNT) double[] borrowed,
    @FixedArray(length = OWNER_COUNT) double[] repaid,
    @FixedArray(length = OWNER_COUNT) double[] interestPaid,
    @FixedArray(length = OWNER_COUNT) double[] writtenOff,
    double interestPool,
    double creditLimit,
    double losses,
    float lendingRate,
    double depositRate,
    float depositLossRatio,
    int bankruptcies) {
}

package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;
import io.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.simulation.Constants.TARIFF_CATEGORY_COUNT;

@Component
public record CountryTradePolicy(
    float tariffRate,
    float tariffFloor,
    @FixedArray(length = TARIFF_CATEGORY_COUNT) float[] categoryTariffRates,
    @FixedArray(length = TARIFF_CATEGORY_COUNT) float[] importShareTrends) {
}

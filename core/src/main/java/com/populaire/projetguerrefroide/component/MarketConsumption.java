package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

@Component
public record MarketConsumption(@FixedArray(length = 36) long[] goodIds, @FixedArray(length = 36) float[] goodAmounts) {
}

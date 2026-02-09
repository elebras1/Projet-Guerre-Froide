package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

@Component
public record BuildingEconomy(float production, float cashReserves, @FixedArray(length = 12) int[] hiredWorkers) {
}

package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

@Component
public record Province(@FixedArray(length = 8) long[] coreIds, long ownerId, long controllerId, long terrainId, int amountChildren, int amountAdults, int amountSeniors) {
}

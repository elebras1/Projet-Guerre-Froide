package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

@Component
public record Law(long lawGroupId, @FixedArray(length = 8) long[] supportIdeologies, @FixedArray(length = 8) long[] opponentIdeologies) {
}

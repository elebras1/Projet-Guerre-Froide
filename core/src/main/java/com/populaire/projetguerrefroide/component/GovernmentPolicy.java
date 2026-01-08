package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

@Component
public record GovernmentPolicy(@FixedArray(length = 4) long[] associatedIdeologies, @FixedArray(length = 32) long[] supportedLaws) {
}

package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

@Component
public record Country(long capitalId, long governmentId, long ideologyId, long identityId, long attitudeId, long headOfStateId, long headOfGovernmentId, @FixedArray(length = 48) long[] lawIds) {
}

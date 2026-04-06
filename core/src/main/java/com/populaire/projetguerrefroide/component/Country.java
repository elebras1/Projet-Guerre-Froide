package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.LAW_COUNT;

@Component
public record Country(long capitalId, long governmentId, long ideologyId, long identityId, long attitudeId, long headOfStateId, long headOfGovernmentId, @FixedArray(length = LAW_COUNT) long[] lawIds) {
}

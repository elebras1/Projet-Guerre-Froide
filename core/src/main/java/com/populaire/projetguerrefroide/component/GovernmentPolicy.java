package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.*;

@Component
public record GovernmentPolicy(
    @FixedArray(length = MAX_GOVERNMENT_ASSOCIATED_IDEOLOGIES) long[] associatedIdeologieIds,
    @FixedArray(length = LAW_COUNT) long[] supportedLawGroupIds,
    @FixedArray(length = LAW_COUNT) long[] supportedLawIds) {
}

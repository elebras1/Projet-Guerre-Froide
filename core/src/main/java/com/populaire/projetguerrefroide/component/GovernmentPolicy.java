package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.LAW_COUNT;
import static com.populaire.projetguerrefroide.util.Constants.MAX_GOVERNMENT_ASSOCIATED_IDEOLOGIES;

@Component
public record GovernmentPolicy(@FixedArray(length = MAX_GOVERNMENT_ASSOCIATED_IDEOLOGIES) long[] associatedIdeologies, @FixedArray(length = LAW_COUNT) long[] supportedLaws) {
}

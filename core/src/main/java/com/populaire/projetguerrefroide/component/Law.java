package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.MAX_LAW_IDEOLOGIES;

@Component
public record Law(long lawGroupId, @FixedArray(length = MAX_LAW_IDEOLOGIES) long[] supportIdeologieIds, @FixedArray(length = MAX_LAW_IDEOLOGIES) long[] opponentIdeologieIds) {
}

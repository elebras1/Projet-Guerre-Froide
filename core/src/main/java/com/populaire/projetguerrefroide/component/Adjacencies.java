package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedArray;

import static com.populaire.projetguerrefroide.util.Constants.MAX_ADJACENCIES;

@Component
public record Adjacencies(@FixedArray(length = MAX_ADJACENCIES) long[] provinceIds) {
}

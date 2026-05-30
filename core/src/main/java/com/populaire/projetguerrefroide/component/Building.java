package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;

@Component
public record Building(long parentId, long typeId, long countryId, int size) {
}

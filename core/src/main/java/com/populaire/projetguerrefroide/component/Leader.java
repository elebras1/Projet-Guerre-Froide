package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;

@Component
public record Leader(String name, int skill, long forceTypeTagId, long traitId, long countryId) {
}

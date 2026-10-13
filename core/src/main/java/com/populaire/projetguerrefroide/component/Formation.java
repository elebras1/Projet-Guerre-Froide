package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;

@Component
public record Formation(long countryId, long forceTypeTagId, long provinceId, long baseId, String nameId) {
}

package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;

@Component
public record Position(int x, int y, long provinceId) {
}

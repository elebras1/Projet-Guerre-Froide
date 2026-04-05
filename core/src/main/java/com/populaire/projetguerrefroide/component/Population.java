package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;

@Component
public record Population(int index, long typeId, long provinceId, long amount, long employment, float consciousness, float militancy, float literacy, float savings) {
}

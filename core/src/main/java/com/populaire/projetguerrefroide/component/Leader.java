package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;

@Component
public record Leader(String name, byte skill, byte forceType) {
}

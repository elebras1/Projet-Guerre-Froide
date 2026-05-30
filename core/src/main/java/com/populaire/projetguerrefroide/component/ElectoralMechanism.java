package com.populaire.projetguerrefroide.component;

import io.github.elebras1.flecs.annotation.Component;

@Component
public record ElectoralMechanism(boolean headOfState, boolean headOfGovernment, int duration) {
}

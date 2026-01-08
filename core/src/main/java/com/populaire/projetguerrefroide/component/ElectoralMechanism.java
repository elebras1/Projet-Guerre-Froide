package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;

@Component
public record ElectoralMechanism(boolean headOfState, boolean headOfGovernment, int duration) {
}

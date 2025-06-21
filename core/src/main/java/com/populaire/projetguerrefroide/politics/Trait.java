package com.populaire.projetguerrefroide.politics;

import com.populaire.projetguerrefroide.entity.Modifier;

public class Trait {
    private final String name;
    private final Modifier modifier;

    public Trait(String name, Modifier modifier) {
        this.name = name;
        this.modifier = modifier;
    }

    public String getName() {
        return this.name;
    }

    public Modifier getModifier() {
        return this.modifier;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trait trait)) return false;

        return this.name.equals(trait.name);
    }
}

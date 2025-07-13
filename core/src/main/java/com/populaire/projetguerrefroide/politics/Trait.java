package com.populaire.projetguerrefroide.politics;

public class Trait {
    private final String name;
    private final int modifierId;

    public Trait(String name, int modifierId) {
        this.name = name;
        this.modifierId = modifierId;
    }

    public String getName() {
        return this.name;
    }

    public int getModifierId() {
        return this.modifierId;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trait trait)) return false;

        return this.name.equals(trait.name);
    }
}

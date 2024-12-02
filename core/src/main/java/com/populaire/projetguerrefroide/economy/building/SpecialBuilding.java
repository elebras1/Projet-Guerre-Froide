package com.populaire.projetguerrefroide.economy.building;

import com.populaire.projetguerrefroide.entity.Modifier;

import java.util.List;

public class SpecialBuilding extends Building{
    private final List<Modifier> modifiers;

    public SpecialBuilding(String name, int initialCost, short time, boolean onMap, boolean visibility, List<Modifier> modifiers) {
        super(name, initialCost, time, onMap, visibility);
        this.modifiers = modifiers;
    }

    public SpecialBuilding(String name, int initialCost, short time, boolean onMap, boolean visibility) {
        super(name, initialCost, time, onMap, visibility);
        this.modifiers = null;
    }

    public List<Modifier> getModifiers() {
        return this.modifiers;
    }

    @Override
    public String toString() {
        return "SpecialBuilding{" +
            "name='" + this.getName() + '\'' +
            ", initialCost=" + this.getInitialCost() +
            ", time=" + this.getTime() +
            ", onMap=" + this.isOnMap() +
            ", visibility=" + this.isVisible() +
            ", modifiers=" + this.modifiers +
            '}';
    }
}

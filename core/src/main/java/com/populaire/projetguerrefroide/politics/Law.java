package com.populaire.projetguerrefroide.politics;

import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.ObjectIntMap;

import java.util.List;

public class Law {
    private final String name;
    private final List<String> requirements;
    private final IntList modifierIds;
    private final ObjectIntMap<Ideology> interestIdeologies;

    public Law(String name, List<String> requirements, IntList modifierIds, ObjectIntMap<Ideology> interestIdeologies) {
        this.name = name;
        this.requirements = requirements;
        this.modifierIds = modifierIds;
        this.interestIdeologies = interestIdeologies;
    }

    public String getName() {
        return this.name;
    }

    public List<String> getRequirements() {
        return this.requirements;
    }

    public IntList getModifierIds() {
        return this.modifierIds;
    }

    public ObjectIntMap<Ideology> getInterestIdeologies() {
        return this.interestIdeologies;
    }

    @Override
    public String toString() {
        return "Law{" +
            "name='" + this.name + '\'' +
            ", requirements=" + this.requirements +
            ", modifiers=" + this.modifierIds +
            ", interestIdeologies=" + this.interestIdeologies +
            '}';
    }
}

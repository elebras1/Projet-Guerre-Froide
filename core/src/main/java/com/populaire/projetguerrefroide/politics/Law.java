package com.populaire.projetguerrefroide.politics;

import com.github.tommyettinger.ds.ObjectIntMap;
import com.populaire.projetguerrefroide.entity.Modifier;

import java.util.List;

public class Law {
    private final String name;
    private final List<String> requirements;
    private final List<Modifier> modifiers;
    private final ObjectIntMap<Ideology> interestIdeologies;

    public Law(String name, List<String> requirements, List<Modifier> modifiers, ObjectIntMap<Ideology> interestIdeologies) {
        this.name = name;
        this.requirements = requirements;
        this.modifiers = modifiers;
        this.interestIdeologies = interestIdeologies;
    }

    public String getName() {
        return this.name;
    }

    public List<String> getRequirements() {
        return this.requirements;
    }

    public List<Modifier> getModifiers() {
        return this.modifiers;
    }

    public ObjectIntMap<Ideology> getInterestIdeologies() {
        return this.interestIdeologies;
    }

    @Override
    public String toString() {
        return "Law{" +
            "name='" + this.name + '\'' +
            ", requirements=" + this.requirements +
            ", modifiers=" + this.modifiers +
            ", interestIdeologies=" + this.interestIdeologies +
            '}';
    }
}

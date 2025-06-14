package com.populaire.projetguerrefroide.politics;

import java.util.List;
import java.util.Objects;

public class Government {
    private final String name;
    private final List<String> ideologiesAcceptance;
    private final Election election;

    public Government(String name, List<String> ideologiesAcceptance, Election election) {
        this.name = name;
        this.ideologiesAcceptance = ideologiesAcceptance;
        this.election = election;
    }

    public Government(String name, List<String> ideologiesAcceptance) {
        this(name, ideologiesAcceptance, null);
    }

    public String getName() {
        return this.name;
    }

    public List<String> getIdeologiesAcceptance() {
        return this.ideologiesAcceptance;
    }

    public Election getElection() {
        return this.election;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Government that)) return false;

        return Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
    }

    @Override
    public String toString() {
        return "Government{" +
            "name='" + this.name + '\'' +
            ", ideologiesAcceptance=" + this.ideologiesAcceptance +
            ", election=" + this.election +
            '}';
    }
}

package com.populaire.projetguerrefroide.politics;

import java.util.List;

public class LawGroup {
    private final String name;
    private final byte factorEnactmentDays;
    private final List<Law> laws;

    public LawGroup(String name, byte factorEnactmentDays, List<Law> laws) {
        this.name = name;
        this.factorEnactmentDays = factorEnactmentDays;
        this.laws = laws;
    }

    public byte getFactorEnactmentDays() {
        return this.factorEnactmentDays;
    }

    public List<Law> getLaws() {
        return this.laws;
    }

    @Override
    public String toString() {
        return "Law{" +
            "name='" + this.name + '\'' +
            "factorEnactmentDays=" + this.factorEnactmentDays +
            ", laws=" + this.laws +
            '}';
    }
}

package com.populaire.projetguerrefroide.politics;

import java.util.List;
import java.util.Map;

public class LawGroup {
    private final String name;
    private final byte factorEnactmentDays;
    private final Map<String, Law> laws;

    public LawGroup(String name, byte factorEnactmentDays, Map<String, Law> laws) {
        this.name = name;
        this.factorEnactmentDays = factorEnactmentDays;
        this.laws = laws;
    }

    public byte getFactorEnactmentDays() {
        return this.factorEnactmentDays;
    }

    public Map<String, Law> getLaws() {
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

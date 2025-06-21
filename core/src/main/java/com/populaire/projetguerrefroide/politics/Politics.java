package com.populaire.projetguerrefroide.politics;

import java.util.Map;

public class Politics {
    private final Map<String, Ideology> ideologies;
    private final Minister[] ministers;
    private final Leader[] leaders;
    private final Map<String, MinisterType> ministerTypes;
    private final Map<String, Government> governments;
    private final Map<String, LawGroup> lawGroups;
    private final byte baseEnactmentDaysLaw;

    public Politics(Map<String, Ideology> ideologies, Minister[] ministers, Leader[] leaders, Map<String, MinisterType> ministerTypes, Map<String, Government> governments, Map<String, LawGroup> lawGroups, byte baseEnactmentDaysLaw) {
        this.ideologies = ideologies;
        this.ministers = ministers;
        this.leaders = leaders;
        this.ministerTypes = ministerTypes;
        this.governments = governments;
        this.lawGroups = lawGroups;
        this.baseEnactmentDaysLaw = baseEnactmentDaysLaw;
    }

    public Map<String, Ideology> getIdeologies() {
        return this.ideologies;
    }

    public Minister[] getMinisters() {
        return this.ministers;
    }

    public Leader[] getLeaders() {
        return this.leaders;
    }

    public Map<String, MinisterType> getMinisterTypes() {
        return this.ministerTypes;
    }

    public Map<String, Government> getGovernments() {
        return this.governments;
    }

    public Map<String, LawGroup> getLawGroups() {
        return this.lawGroups;
    }

    public Minister getMinister(int index) {
        if (index < 0 || index >= this.ministers.length) {
            throw new IndexOutOfBoundsException("Invalid minister index: " + index);
        }
        return this.ministers[index];
    }
}

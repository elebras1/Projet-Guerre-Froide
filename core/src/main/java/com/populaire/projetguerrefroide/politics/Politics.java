package com.populaire.projetguerrefroide.politics;

import java.util.List;
import java.util.Map;

public class Politics {
    private final Map<String, Ideology> ideologies;
    private final Minister[] ministers;
    private final Map<String, MinisterType> ministerTypes;
    private final Map<String, Government> governments;
    private final List<LawGroup> lawGroups;
    private final byte baseEnactmentDaysLaw;

    public Politics(Map<String, Ideology> ideologies, Minister[] ministers, Map<String, MinisterType> ministerTypes, Map<String, Government> governments, List<LawGroup> lawGroups, byte baseEnactmentDaysLaw) {
        this.ideologies = ideologies;
        this.ministers = ministers;
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

    public Map<String, MinisterType> getMinisterTypes() {
        return this.ministerTypes;
    }

    public Map<String, Government> getGovernments() {
        return this.governments;
    }

    public List<LawGroup> getLawGroups() {
        return this.lawGroups;
    }

    public Minister getMinister(int index) {
        if (index < 0 || index >= this.ministers.length) {
            throw new IndexOutOfBoundsException("Invalid minister index: " + index);
        }
        return this.ministers[index];
    }
}

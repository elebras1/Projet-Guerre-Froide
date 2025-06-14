package com.populaire.projetguerrefroide.politics;

import java.util.Map;

public class Politics {
    private final Map<String, Ideology> ideologies;
    private final Minister[] ministers;
    private final Map<String, MinisterType> ministerTypes;
    private final Map<String, Government> governments;

    public Politics(Map<String, Ideology> ideologies, Minister[] ministers, Map<String, MinisterType> ministerTypes, Map<String, Government> governments) {
        this.ideologies = ideologies;
        this.ministers = ministers;
        this.ministerTypes = ministerTypes;
        this.governments = governments;
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

    public Minister getMinister(int index) {
        if (index < 0 || index >= ministers.length) {
            throw new IndexOutOfBoundsException("Invalid minister index: " + index);
        }
        return ministers[index];
    }
}

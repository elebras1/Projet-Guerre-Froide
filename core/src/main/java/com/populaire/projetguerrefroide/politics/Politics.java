package com.populaire.projetguerrefroide.politics;

import java.util.Map;

public class Politics {
    private final Map<String, LawGroup> lawGroups;
    private final byte baseEnactmentDaysLaw;

    public Politics(Map<String, LawGroup> lawGroups, byte baseEnactmentDaysLaw) {
        this.lawGroups = lawGroups;
        this.baseEnactmentDaysLaw = baseEnactmentDaysLaw;
    }

    public Map<String, LawGroup> getLawGroups() {
        return this.lawGroups;
    }

    public byte getBaseEnactmentDaysLaw() {
        return this.baseEnactmentDaysLaw;
    }
}

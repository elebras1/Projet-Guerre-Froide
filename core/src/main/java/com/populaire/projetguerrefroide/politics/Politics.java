package com.populaire.projetguerrefroide.politics;

import java.util.Map;

public class Politics {
    private final Leader[] leaders;
    private final Map<String, Government> governments;
    private final Map<String, LawGroup> lawGroups;
    private final byte baseEnactmentDaysLaw;

    public Politics(Leader[] leaders, Map<String, Government> governments, Map<String, LawGroup> lawGroups, byte baseEnactmentDaysLaw) {
        this.leaders = leaders;
        this.governments = governments;
        this.lawGroups = lawGroups;
        this.baseEnactmentDaysLaw = baseEnactmentDaysLaw;
    }

    public Leader[] getLeaders() {
        return this.leaders;
    }

    public Map<String, Government> getGovernments() {
        return this.governments;
    }

    public Map<String, LawGroup> getLawGroups() {
        return this.lawGroups;
    }

    public byte getBaseEnactmentDaysLaw() {
        return this.baseEnactmentDaysLaw;
    }
}

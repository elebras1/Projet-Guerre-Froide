package com.populaire.projetguerrefroide.politics;

import com.github.tommyettinger.ds.IntLongMap;

import java.util.Map;

public class Politics {
    private final IntLongMap ministersIds;
    private final Map<String, Ideology> ideologies;
    private final Leader[] leaders;
    private final Map<String, MinisterType> ministerTypes;
    private final Map<String, Government> governments;
    private final Map<String, LawGroup> lawGroups;
    private final byte baseEnactmentDaysLaw;

    public Politics(IntLongMap ministersIds, Map<String, Ideology> ideologies, Leader[] leaders, Map<String, MinisterType> ministerTypes, Map<String, Government> governments, Map<String, LawGroup> lawGroups, byte baseEnactmentDaysLaw) {
        this.ministersIds = ministersIds;
        this.ideologies = ideologies;
        this.leaders = leaders;
        this.ministerTypes = ministerTypes;
        this.governments = governments;
        this.lawGroups = lawGroups;
        this.baseEnactmentDaysLaw = baseEnactmentDaysLaw;
    }

    public IntLongMap getMinistersIds() {
        return this.ministersIds;
    }

    public Map<String, Ideology> getIdeologies() {
        return this.ideologies;
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
}

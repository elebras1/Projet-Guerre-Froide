package com.populaire.projetguerrefroide.national;

import com.github.tommyettinger.ds.ObjectIntMap;

import java.util.Map;

public class NationalIdeas {
    private final ObjectIntMap<String> cultureIds;
    private final ObjectIntMap<String> religionIds;
    private final Map<String, Identity> identities;
    private final Map<String, Attitude> attitudes;

    public NationalIdeas(ObjectIntMap<String> cultureIds, ObjectIntMap<String> religionIds, Map<String, Identity> identities, Map<String, Attitude> attitudes) {
        this.cultureIds = cultureIds;
        this.religionIds = religionIds;
        this.identities = identities;
        this.attitudes = attitudes;
    }

    public ObjectIntMap<String> getCultureIds() {
        return this.cultureIds;
    }

    public ObjectIntMap<String> getReligionIds() {
        return this.religionIds;
    }

    public Map<String, Identity> getIdentities() {
        return this.identities;
    }

    public Map<String, Attitude> getAttitudes() {
        return this.attitudes;
    }

    @Override
    public String toString() {
        return "NationalIdeas{" +
                "cultureIds=" + this.cultureIds +
                ", religionIds=" + this.religionIds +
                ", identities=" + this.identities +
                ", attitudes=" + this.attitudes +
                '}';
    }
}

package com.populaire.projetguerrefroide.national;

import com.github.tommyettinger.ds.ObjectIntMap;

import java.util.Map;

public class NationalIdeas {
    private final CultureStore cultureStore;
    private final ReligionStore religionStore;
    private final ObjectIntMap<String> cultureIds;
    private final ObjectIntMap<String> religionIds;
    private final Map<String, Identity> identities;

    public NationalIdeas(CultureStore cultureStore, ReligionStore religionStore, ObjectIntMap<String> cultureIds, ObjectIntMap<String> religionIds, Map<String, Identity> identities) {
        this.cultureStore = cultureStore;
        this.religionStore = religionStore;
        this.cultureIds = cultureIds;
        this.religionIds = religionIds;
        this.identities = identities;
    }

    public CultureStore getCultureStore() {
        return this.cultureStore;
    }

    public ReligionStore getReligionStore() {
        return this.religionStore;
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

    @Override
    public String toString() {
        return "NationalIdeas{" +
                "cultureStore=" + this.cultureStore +
                ", religionStore=" + this.religionStore +
                "cultureIds=" + this.cultureIds +
                ", religionIds=" + this.religionIds +
                ", identities=" + this.identities +
                '}';
    }
}

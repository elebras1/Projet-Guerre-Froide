package com.populaire.projetguerrefroide.national;

import com.github.tommyettinger.ds.ObjectIntMap;

import java.util.Map;

public class NationalIdeas {
    private final CultureStore cultureStore;
    private final ReligionStore religionStore;
    private final ObjectIntMap<String> cultureIds;
    private final ObjectIntMap<String> religionIds;

    public NationalIdeas(CultureStore cultureStore, ReligionStore religionStore, ObjectIntMap<String> cultureIds, ObjectIntMap<String> religionIds) {
        this.cultureStore = cultureStore;
        this.religionStore = religionStore;
        this.cultureIds = cultureIds;
        this.religionIds = religionIds;
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

    @Override
    public String toString() {
        return "NationalIdeas{" +
                "cultureStore=" + this.cultureStore +
                ", religionStore=" + this.religionStore +
                "cultureIds=" + this.cultureIds +
                ", religionIds=" + this.religionIds +
                '}';
    }
}

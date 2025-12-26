package com.populaire.projetguerrefroide.national;

import com.github.tommyettinger.ds.ObjectIntMap;

public class NationalIdeas {
    private final ReligionStore religionStore;
    private final ObjectIntMap<String> religionIds;

    public NationalIdeas(ReligionStore religionStore, ObjectIntMap<String> religionIds) {
        this.religionStore = religionStore;
        this.religionIds = religionIds;
    }

    public ReligionStore getReligionStore() {
        return this.religionStore;
    }

    public ObjectIntMap<String> getReligionIds() {
        return this.religionIds;
    }

    @Override
    public String toString() {
        return "NationalIdeas{" +
                ", religionStore=" + this.religionStore +
                ", religionIds=" + this.religionIds +
                '}';
    }
}

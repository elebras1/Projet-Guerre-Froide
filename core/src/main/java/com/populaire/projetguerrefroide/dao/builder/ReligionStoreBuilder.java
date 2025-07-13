package com.populaire.projetguerrefroide.dao.builder;

import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.national.ReligionStore;

import java.util.List;

public class ReligionStoreBuilder {
    private final int defaultCapacity;
    private int index;
    private final List<String> names;
    private final IntList colors;
    private final IntList modifierIds;
    private final IntList modifierStart;
    private final IntList modifierCount;

    public ReligionStoreBuilder() {
        this.defaultCapacity = 16;
        this.index = 0;
        this.names = new ObjectList<>(this.defaultCapacity);
        this.colors = new IntList(this.defaultCapacity);
        this.modifierIds = new IntList();
        this.modifierStart = new IntList(this.defaultCapacity);
        this.modifierCount = new IntList(this.defaultCapacity);
    }

    public int getDefaultCapacity() {
        return this.defaultCapacity;
    }

    public int getIndex() {
        return this.index;
    }

    public ReligionStoreBuilder addReligion(String name, int color) {
        this.names.add(name);
        this.colors.add(color);

        this.modifierStart.add(this.modifierIds.size());
        this.modifierCount.add(0);

        this.index = this.names.size() - 1;
        return this;
    }

    public ReligionStoreBuilder addModifier(int modifierId) {
        this.modifierIds.add(modifierId);

        int currentCount = this.modifierCount.get(this.index);
        this.modifierCount.set(this.index, currentCount + 1);

        return this;
    }

    public ReligionStore build() {
        return new ReligionStore(this.names, this.colors, this.modifierIds, this.modifierStart, this.modifierCount);
    }
}

package com.populaire.projetguerrefroide.util.collection;

import com.github.tommyettinger.ds.LongObjectMap;

public class LongObjectSlotMap<V> extends LongObjectMap<V> {

    public int slotCount() {
        return this.keyTable.length;
    }

    public long slotKey(int slot) {
        return this.keyTable[slot];
    }

    public V slotValue(int slot) {
        return this.valueTable[slot];
    }
}

package com.populaire.projetguerrefroide.util.collection;

import com.github.tommyettinger.ds.LongIntMap;

public class LongIntSlotMap extends LongIntMap {

    public int slotCount() {
        return this.keyTable.length;
    }

    public long slotKey(int slot) {
        return this.keyTable[slot];
    }

    public int slotValue(int slot) {
        return this.valueTable[slot];
    }
}

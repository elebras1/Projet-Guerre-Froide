package com.populaire.projetguerrefroide.util.collection;

import com.github.tommyettinger.ds.LongFloatMap;

public class LongFloatSlotMap extends LongFloatMap {

    public int slotCount() {
        return this.keyTable.length;
    }

    public long slotKey(int slot) {
        return this.keyTable[slot];
    }

    public float slotValue(int slot) {
        return this.valueTable[slot];
    }
}

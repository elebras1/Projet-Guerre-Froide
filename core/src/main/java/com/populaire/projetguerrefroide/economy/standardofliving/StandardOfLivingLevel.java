package com.populaire.projetguerrefroide.economy.standardofliving;

import com.github.tommyettinger.ds.ObjectFloatMap;
import com.populaire.projetguerrefroide.economy.good.Good;

public class StandardOfLivingLevel {
    private final byte level;
    private final ObjectFloatMap<Good> demands;

    public StandardOfLivingLevel(byte level, ObjectFloatMap<Good> demands) {
        this.level = level;
        this.demands = demands;
    }

    public byte getLevel() {
        return this.level;
    }

    public ObjectFloatMap<Good> getDemands() {
        return this.demands;
    }

    @Override
    public String toString() {
        return "StandardOfLivingLevel{" +
            "level=" + this.level +
            ", demands=" + this.demands +
            '}';
    }
}

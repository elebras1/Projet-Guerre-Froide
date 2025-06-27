package com.populaire.projetguerrefroide.economy.good;

public enum GoodType {
    RESOURCE((byte) 0),
    ADVANCED((byte) 1),
    MILITARY((byte) 2);

    private final byte id;

    private static final GoodType[] GOOD_TYPES = new GoodType[values().length];

    static {
        for (GoodType type : values()) {
            GOOD_TYPES[type.id] = type;
        }
    }

    GoodType(byte id) {
        this.id = id;
    }

    public byte getId() {
        return this.id;
    }

    public static GoodType fromId(byte id) {
        if (id < 0 || id >= GOOD_TYPES.length) {
            throw new IllegalArgumentException("Invalid GoodType id: " + id);
        }
        return GOOD_TYPES[id];
    }
}

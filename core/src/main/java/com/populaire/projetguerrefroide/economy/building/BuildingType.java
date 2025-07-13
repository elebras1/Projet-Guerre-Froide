package com.populaire.projetguerrefroide.economy.building;

public enum BuildingType {
    ECONOMY((byte) 0),
    DEVELOPMENT((byte) 1),
    SPECIAL((byte) 2);

    private final byte id;

    private static final BuildingType[] BUILDING_TYPES = new BuildingType[values().length];

    static {
        for (BuildingType type : values()) {
            BUILDING_TYPES[type.id] = type;
        }
    }

    BuildingType(byte id) {
        this.id = id;
    }

    public byte getId() {
        return this.id;
    }

    public static BuildingType fromId(byte id) {
        if (id < 0 || id >= BUILDING_TYPES.length) {
            throw new IllegalArgumentException("Invalid BuildingType id: " + id);
        }
        return BUILDING_TYPES[id];
    }
}

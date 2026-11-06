package com.populaire.projetguerrefroide.protocol.event;

public enum EventKind {
    BUILDING_LEVEL_UP,
    COMMAND_REJECTED;

    private static final EventKind[] VALUES = values();

    public static EventKind of(int ordinal) {
        return VALUES[ordinal];
    }
}

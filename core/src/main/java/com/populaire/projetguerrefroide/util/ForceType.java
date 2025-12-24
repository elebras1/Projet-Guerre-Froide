package com.populaire.projetguerrefroide.util;

public enum ForceType {
    LAND, SEA, AIR;

    public static byte fromString(String type) {
        return switch(type) {
            case "land" -> 0;
            case "sea" -> 1;
            case "air" -> 2;
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }

    public static ForceType fromByte(byte type) {
        return switch(type) {
            case 0 -> LAND;
            case 1 -> SEA;
            case 2 -> AIR;
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }
}


package com.populaire.projetguerrefroide.politics;

public enum ForceType {
    LAND, SEA, AIR;

    public static ForceType fromString(String type) {
        return switch(type) {
            case "land" -> LAND;
            case "sea" -> SEA;
            case "air" -> AIR;
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }
}


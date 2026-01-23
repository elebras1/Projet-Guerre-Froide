package com.populaire.projetguerrefroide.util;

public class ForceTypeUtils {
    public static byte fromString(String type) {
        return switch(type) {
            case "land" -> 0;
            case "sea" -> 1;
            case "air" -> 2;
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }
}


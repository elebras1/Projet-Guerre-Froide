package com.populaire.projetguerrefroide.util;

public class ForceTypeUtils {
    public static final int LAND_FORCE_TYPE = 0;
    public static final int SEA_FORCE_TYPE = 1;
    public static final int AIR_FORCE_TYPE = 2;

    public static int getForceType(String forceType) {
        return switch (forceType) {
            case "land" -> LAND_FORCE_TYPE;
            case "sea" -> SEA_FORCE_TYPE;
            case "air" -> AIR_FORCE_TYPE;
            default -> throw new IllegalArgumentException("Unknown force type : " + forceType);
        };
    }
}

package com.populaire.projetguerrefroide.util;

public class StrataUtils {
    public static final int POOR_STRATA = 0;
    public static final int MIDDLE_STRATA = 1;
    public static final int RICH_STRATA = 2;

    public static int getStrata(String strata) {
        return switch (strata) {
            case "poor" -> POOR_STRATA;
            case "middle" -> MIDDLE_STRATA;
            case "rich" -> RICH_STRATA;
            default -> throw new IllegalArgumentException("Unknown strata : " + strata);
        };
    }
}

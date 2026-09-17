package com.populaire.projetguerrefroide.util;

public class IncomeTypeUtils {
    public static final int NO_INCOME_TYPE = 0;
    public static final int ADMINISTRATION_INCOME_TYPE = 1;
    public static final int MILITARY_INCOME_TYPE = 2;
    public static final int EDUCATION_INCOME_TYPE = 3;

    public static int getIncomeType(String incomeType) {
        return switch (incomeType) {
            case "none" -> NO_INCOME_TYPE;
            case "administration" -> ADMINISTRATION_INCOME_TYPE;
            case "military" -> MILITARY_INCOME_TYPE;
            case "education" -> EDUCATION_INCOME_TYPE;
            default -> throw new IllegalArgumentException("Unknown income type : " + incomeType);
        };
    }
}

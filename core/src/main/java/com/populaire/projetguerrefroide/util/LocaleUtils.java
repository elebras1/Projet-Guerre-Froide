package com.populaire.projetguerrefroide.util;

import java.util.Locale;

public class LocaleUtils {
    private static Locale LOCALE;

    public static Locale getLocale(String language) {
        if(LOCALE != null) {
            return LOCALE;
        }

        LOCALE = switch (language) {
            case "ENGLISH" -> Locale.ENGLISH;
            case "FRENCH" -> Locale.FRENCH;
            case "GERMAN" -> Locale.GERMAN;
            case "POLSKI" -> Locale.forLanguageTag("pl");
            case "SPANISH" -> Locale.forLanguageTag("es");
            case "ITALIAN" -> Locale.ITALIAN;
            case "SWEDISH" -> Locale.forLanguageTag("sv");
            case "CZECH" -> Locale.forLanguageTag("cs");
            case "HUNGARIAN" -> Locale.forLanguageTag("hu");
            case "DUTCH" -> Locale.forLanguageTag("nl");
            case "PORTUGUESE" -> Locale.forLanguageTag("pt");
            case "RUSSIAN" -> Locale.forLanguageTag("ru");
            case "FINNISH" -> Locale.forLanguageTag("fi");
            case "CHINESE" -> Locale.CHINA;
            default -> null;
        };

        return LOCALE;
    }
}

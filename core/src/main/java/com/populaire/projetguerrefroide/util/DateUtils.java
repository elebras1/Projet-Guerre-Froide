package com.populaire.projetguerrefroide.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

public class DateUtils {
    private static DateTimeFormatter DATE_TIME_FORMATTER;

    public static String formatDate(LocalDate date, Map<String, String> localisation, String language) {
        Locale locale = LocaleUtils.getLocale(language);
        String pattern = localisation.get("DATE_FORMAT");
        if(pattern == null || locale == null) {
            return date.toString();
        }

        if(DATE_TIME_FORMATTER == null) {
            DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(pattern, locale);
        }

        return date.format(DATE_TIME_FORMATTER);
    }
}

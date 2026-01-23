package com.populaire.projetguerrefroide.util;

import java.util.Map;

public class ValueFormatter {
    public static String format(long value, Map<String, String> localisation) {
        if (value >= 1_000_000_000 || value <= -1_000_000_000) {
            return String.format("%.2f" + localisation.get("BILLION"), value / 1_000_000_000.0);
        } else if (value >= 1_000_000 || value <= -1_000_000) {
            return String.format("%.2f" + localisation.get("MILION"), value / 1_000_000.0);
        } else if (value >= 1_000 || value <= -1_000) {
            return String.format("%.2f" + localisation.get("THOUSAND"), value / 1_000.0);
        } else {
            return String.valueOf(value);
        }
    }

    public static String format(float value) {
        return String.format("%.4f", value);
    }
}

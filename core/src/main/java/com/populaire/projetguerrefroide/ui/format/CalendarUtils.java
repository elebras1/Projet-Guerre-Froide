package com.populaire.projetguerrefroide.ui.format;

public final class CalendarUtils {
    private CalendarUtils() {
    }

    public static int year(long epochDay) {
        long shifted = epochDay + 719468;
        long era = Math.floorDiv(shifted, 146097);
        long dayOfEra = shifted - era * 146097;
        long yearOfEra = (dayOfEra - dayOfEra / 1460 + dayOfEra / 36524 - dayOfEra / 146096) / 365;
        long dayOfYear = dayOfEra - (365 * yearOfEra + yearOfEra / 4 - yearOfEra / 100);
        long monthIndex = (5 * dayOfYear + 2) / 153;
        long month = monthIndex < 10 ? monthIndex + 3 : monthIndex - 9;
        return (int) (yearOfEra + era * 400 + (month <= 2 ? 1 : 0));
    }

    public static int month(long epochDay) {
        long shifted = epochDay + 719468;
        long era = Math.floorDiv(shifted, 146097);
        long dayOfEra = shifted - era * 146097;
        long yearOfEra = (dayOfEra - dayOfEra / 1460 + dayOfEra / 36524 - dayOfEra / 146096) / 365;
        long dayOfYear = dayOfEra - (365 * yearOfEra + yearOfEra / 4 - yearOfEra / 100);
        long monthIndex = (5 * dayOfYear + 2) / 153;
        return (int) (monthIndex < 10 ? monthIndex + 3 : monthIndex - 9);
    }

    public static int dayOfMonth(long epochDay) {
        long shifted = epochDay + 719468;
        long era = Math.floorDiv(shifted, 146097);
        long dayOfEra = shifted - era * 146097;
        long yearOfEra = (dayOfEra - dayOfEra / 1460 + dayOfEra / 36524 - dayOfEra / 146096) / 365;
        long dayOfYear = dayOfEra - (365 * yearOfEra + yearOfEra / 4 - yearOfEra / 100);
        long monthIndex = (5 * dayOfYear + 2) / 153;
        return (int) (dayOfYear - (153 * monthIndex + 2) / 5 + 1);
    }

    public static int monthKey(long epochDay) {
        return year(epochDay) * 12 + month(epochDay) - 1;
    }
}

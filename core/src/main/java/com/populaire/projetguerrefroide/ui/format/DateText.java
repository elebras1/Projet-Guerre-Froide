package com.populaire.projetguerrefroide.ui.format;

import com.badlogic.gdx.utils.CharArray;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.util.CalendarUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public final class DateText {
    private static final int LITERAL = 0;
    private static final int DAY = 1;
    private static final int MONTH_NUMBER = 2;
    private static final int MONTH_TEXT = 3;
    private static final int YEAR = 4;
    private static final int YEAR_TWO_DIGITS = 5;
    private final int[] kinds;
    private final int[] widths;
    private final String[] literals;
    private final String[] monthNames;

    public DateText(String pattern, Locale locale) {
        IntList kindList = new IntList();
        IntList widthList = new IntList();
        List<String> literalList = new ObjectList<>();
        String monthPattern = "MMMM";
        if (pattern == null || locale == null) {
            pattern = "yyyy-MM-dd";
            locale = Locale.ROOT;
        }
        int position = 0;
        while (position < pattern.length()) {
            char character = pattern.charAt(position);
            if (character == '\'') {
                StringBuilder literal = new StringBuilder();
                position++;
                while (position < pattern.length()) {
                    char quoted = pattern.charAt(position);
                    if (quoted == '\'') {
                        if (position + 1 < pattern.length() && pattern.charAt(position + 1) == '\'') {
                            literal.append('\'');
                            position += 2;
                            continue;
                        }
                        position++;
                        break;
                    }
                    literal.append(quoted);
                    position++;
                }
                if (literal.isEmpty()) {
                    literal.append('\'');
                }
                add(kindList, widthList, literalList, LITERAL, 0, literal.toString());
                continue;
            }
            if (Character.isLetter(character)) {
                int run = 1;
                while (position + run < pattern.length() && pattern.charAt(position + run) == character) {
                    run++;
                }
                switch (character) {
                    case 'd' -> add(kindList, widthList, literalList, DAY, run, null);
                    case 'M', 'L' -> {
                        if (run >= 3) {
                            monthPattern = run == 3 ? "MMM" : "MMMM";
                            add(kindList, widthList, literalList, MONTH_TEXT, run, null);
                        } else {
                            add(kindList, widthList, literalList, MONTH_NUMBER, run, null);
                        }
                    }
                    case 'y', 'u' -> add(kindList, widthList, literalList, run == 2 ? YEAR_TWO_DIGITS : YEAR, run, null);
                    default -> add(kindList, widthList, literalList, LITERAL, 0, pattern.substring(position, position + run));
                }
                position += run;
                continue;
            }
            add(kindList, widthList, literalList, LITERAL, 0, String.valueOf(character));
            position++;
        }
        this.kinds = kindList.toArray();
        this.widths = widthList.toArray();
        this.literals = literalList.toArray(new String[0]);
        this.monthNames = new String[12];
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern(monthPattern, locale);
        for (int month = 1; month <= 12; month++) {
            this.monthNames[month - 1] = LocalDate.of(2000, month, 1).format(monthFormatter);
        }
    }

    private static void add(IntList kinds, IntList widths, List<String> literals, int kind, int width, String literal) {
        kinds.add(kind);
        widths.add(width);
        literals.add(literal);
    }

    public CharArray format(CharArray out, long epochDay) {
        out.clear();
        int year = CalendarUtils.year(epochDay);
        int month = CalendarUtils.month(epochDay);
        int day = CalendarUtils.dayOfMonth(epochDay);
        for (int i = 0; i < this.kinds.length; i++) {
            switch (this.kinds[i]) {
                case LITERAL -> out.append(this.literals[i]);
                case DAY -> out.append(day, this.widths[i]);
                case MONTH_NUMBER -> out.append(month, this.widths[i]);
                case MONTH_TEXT -> out.append(this.monthNames[month - 1]);
                case YEAR_TWO_DIGITS -> out.append(Math.floorMod(year, 100), 2);
                default -> out.append(year, this.widths[i]);
            }
        }
        return out;
    }
}

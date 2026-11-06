package com.populaire.projetguerrefroide.ui.format;

import com.badlogic.gdx.utils.CharArray;

import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;

public final class TextFormat {
    private static final long[] POWERS_OF_TEN = {1L, 10L, 100L, 1_000L, 10_000L, 100_000L, 1_000_000L, 10_000_000L, 100_000_000L};
    private final String thousand;
    private final String million;
    private final String billion;
    private final char decimalSeparator;

    public TextFormat(Map<String, String> localisation) {
        this.thousand = nonNull(localisation.get("THOUSAND"));
        this.million = nonNull(localisation.get("MILION"));
        this.billion = nonNull(localisation.get("BILLION"));
        this.decimalSeparator = DecimalFormatSymbols.getInstance(Locale.getDefault(Locale.Category.FORMAT)).getDecimalSeparator();
    }

    private static String nonNull(String value) {
        return value == null ? "" : value;
    }

    public CharArray compact(CharArray out, long value) {
        out.clear();
        return this.appendCompact(out, value);
    }

    public CharArray appendCompact(CharArray out, long value) {
        if (value >= 1_000_000_000L || value <= -1_000_000_000L) {
            this.appendFixed(out, value / 1_000_000_000.0, 2);
            return out.append(this.billion);
        }
        if (value >= 1_000_000L || value <= -1_000_000L) {
            this.appendFixed(out, value / 1_000_000.0, 2);
            return out.append(this.million);
        }
        if (value >= 1_000L || value <= -1_000L) {
            this.appendFixed(out, value / 1_000.0, 2);
            return out.append(this.thousand);
        }
        return out.append(value);
    }

    public CharArray fixed(CharArray out, double value, int decimals) {
        out.clear();
        return this.appendFixed(out, value, decimals);
    }

    public CharArray appendFixed(CharArray out, double value, int decimals) {
        return appendDecimal(out, value, decimals, this.decimalSeparator);
    }

    public static CharArray appendDecimal(CharArray out, double value, int decimals, char separator) {
        if (Double.isNaN(value)) {
            return out.append("NaN");
        }
        if (Double.isInfinite(value)) {
            return out.append(value > 0 ? "Infinity" : "-Infinity");
        }
        long scale = POWERS_OF_TEN[decimals];
        long scaled = (long) Math.floor(Math.abs(value) * scale + 0.5);
        if (value < 0) {
            out.append('-');
        }
        out.append(scaled / scale);
        if (decimals > 0) {
            out.append(separator);
            out.append(scaled % scale, decimals);
        }
        return out;
    }

    public static CharArray integer(CharArray out, long value) {
        out.clear();
        return out.append(value);
    }
}

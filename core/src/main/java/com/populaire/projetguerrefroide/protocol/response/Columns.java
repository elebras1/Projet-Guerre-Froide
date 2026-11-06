package com.populaire.projetguerrefroide.protocol.response;

import java.util.Arrays;

public final class Columns {
    private Columns() {
    }

    public static int[] fit(int[] column, int size) {
        if (column.length >= size) {
            return column;
        }
        return Arrays.copyOf(column, Math.max(size, column.length * 2));
    }

    public static long[] fit(long[] column, int size) {
        if (column.length >= size) {
            return column;
        }
        return Arrays.copyOf(column, Math.max(size, column.length * 2));
    }

    public static float[] fit(float[] column, int size) {
        if (column.length >= size) {
            return column;
        }
        return Arrays.copyOf(column, Math.max(size, column.length * 2));
    }

    public static boolean[] fit(boolean[] column, int size) {
        if (column.length >= size) {
            return column;
        }
        return Arrays.copyOf(column, Math.max(size, column.length * 2));
    }
}

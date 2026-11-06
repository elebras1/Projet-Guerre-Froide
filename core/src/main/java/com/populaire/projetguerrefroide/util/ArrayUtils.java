package com.populaire.projetguerrefroide.util;

import java.util.Arrays;

public class ArrayUtils {

    public static float[] zeroed(float[] array) {
        Arrays.fill(array, 0f);
        return array;
    }

    public static double[] zeroed(double[] array) {
        Arrays.fill(array, 0.0);
        return array;
    }

    public static int[] zeroed(int[] array) {
        Arrays.fill(array, 0);
        return array;
    }

    public static boolean[] zeroed(boolean[] array) {
        Arrays.fill(array, false);
        return array;
    }
}

package com.populaire.projetguerrefroide.util;

public class EmploymentUtils {

    public static int workerTarget(int workforce, int size, float popTypeRatio, float scale) {
        return (int) (workforce * size * popTypeRatio * scale);
    }

    public static int rationedWorkers(int target, int available, int demand) {
        if (demand <= available) {
            return target;
        }
        return (int) ((long) target * available / demand);
    }
}

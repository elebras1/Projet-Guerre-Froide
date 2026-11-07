package com.populaire.projetguerrefroide.system.population;

public class EmploymentUtils {

    public static int workerTarget(int workforce, int size, float popTypeRatio, float scale) {
        return (int) (workforce * size * popTypeRatio * scale);
    }

    public static float activeShare(float baseShare, float workforceShareModifier) {
        return Math.min(1f, baseShare * Math.max(0f, 1f + workforceShareModifier));
    }

    public static int activeWorkers(int total, float workforceShare) {
        return (int) (total * workforceShare);
    }

    public static int rationedWorkers(int target, int available, int demand) {
        if (demand <= available) {
            return target;
        }
        return (int) ((long) target * available / demand);
    }
}

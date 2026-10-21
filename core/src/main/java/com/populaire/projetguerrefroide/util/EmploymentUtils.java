package com.populaire.projetguerrefroide.util;

import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.GlobalPopulationType;
import com.populaire.projetguerrefroide.component.GlobalPopulationTypeView;

import static com.populaire.projetguerrefroide.util.Constants.POP_TYPE_COUNT;

public class EmploymentUtils {

    public static int workerTarget(int workforce, int size, float popTypeRatio, float scale) {
        return (int) (workforce * size * popTypeRatio * scale);
    }

    public static float[] baseWorkforceShares(World world) {
        GlobalPopulationTypeView globalPopulationType = world.obtainEntityView(world.lookup("global_population_type")).getView(GlobalPopulationType.class);
        float[] baseShares = new float[POP_TYPE_COUNT];
        for (int p = 0; p < POP_TYPE_COUNT; p++) {
            baseShares[p] = globalPopulationType.workforceShareByPopType(p);
        }
        return baseShares;
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

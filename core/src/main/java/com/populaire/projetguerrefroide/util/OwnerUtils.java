package com.populaire.projetguerrefroide.util;

import static com.populaire.projetguerrefroide.util.Constants.*;

public class OwnerUtils {

    public static int ownerIndex(long ownerTagId, EcsConstants ecsConstants) {
        if (ownerTagId == ecsConstants.capitalistTag()) {
            return CAPITALIST_OWNER;
        }
        if (ownerTagId == ecsConstants.workersTag()) {
            return WORKERS_OWNER;
        }
        return STATE_OWNER;
    }

    public static long ownerTag(float capitalistShareRate, float workerShareRate, float stateShareRate, EcsConstants ecsConstants) {
        if (capitalistShareRate > 0f) {
            return ecsConstants.capitalistTag();
        }
        if (workerShareRate > stateShareRate) {
            return ecsConstants.workersTag();
        }
        return ecsConstants.countryTag();
    }
}

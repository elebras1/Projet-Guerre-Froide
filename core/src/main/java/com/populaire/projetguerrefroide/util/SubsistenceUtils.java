package com.populaire.projetguerrefroide.util;

import static com.populaire.projetguerrefroide.util.Constants.SUBSISTENCE_LIFE_SHARE;

public class SubsistenceUtils {

    public static float coveredLifeShare(boolean rural, int amount, int employment) {
        if (!rural || amount <= 0) {
            return 0f;
        }
        return SUBSISTENCE_LIFE_SHARE * (amount - employment) / amount;
    }
}

package com.populaire.projetguerrefroide.system.population;

import static com.populaire.projetguerrefroide.simulation.Constants.FAMINE_DEATH_RATE;
import static com.populaire.projetguerrefroide.simulation.Constants.SUBSISTENCE_LIFE_SHARE;

public class SubsistenceUtils {

    public static float coveredLifeShare(boolean rural, int amount, int employment) {
        if (!rural || amount <= 0) {
            return 0f;
        }
        return SUBSISTENCE_LIFE_SHARE * (amount - employment) / amount;
    }

    public static float famineDeathRate(float lifeNeedsSatisfaction) {
        return FAMINE_DEATH_RATE * Math.max(0f, SUBSISTENCE_LIFE_SHARE - lifeNeedsSatisfaction) / SUBSISTENCE_LIFE_SHARE;
    }
}

package com.populaire.projetguerrefroide.system.rgo;

import com.populaire.projetguerrefroide.component.CountryMarketView;
import com.populaire.projetguerrefroide.component.ResourceGatheringView;

import static com.populaire.projetguerrefroide.simulation.Constants.NEEDS_SCALING_FACTOR;
import static com.populaire.projetguerrefroide.simulation.Constants.RGO_MECHANIZATION_LABOUR_SAVING;
import static com.populaire.projetguerrefroide.simulation.Constants.RURAL_WAGE_SHARE;

public class RgoUtils {

    public static float wageBill(CountryMarketView countryMarket, int workerPopTypeIndex, float minWageFactor, int workers) {
        float wagePerUnit = RURAL_WAGE_SHARE * (countryMarket.lifeCostsByPopType(workerPopTypeIndex) + 0.2f * countryMarket.everydayCostsByPopType(workerPopTypeIndex)) * (1f + minWageFactor);
        return wagePerUnit * workers / NEEDS_SCALING_FACTOR;
    }

    /**
     * Workers an endowment RGO employs (annex 12, decision 192): all the active workers of its type when it is not
     * mechanized beyond its 1946 level (family farm), active / (1 + k x effective added level) when it is; the others are
     * freed. The 1946 levels are already in the 1946 pop structure (too many farmers there is decision 131's subject).
     */
    public static float addedLevel(ResourceGatheringView rgo) {
        return Math.max(0, rgo.mechanization() - rgo.mechanizationBase()) * rgo.upkeepServed();
    }

    /** Workers of a family farm (annex 12, decision 197): the needed workers times the share of them that work the farm. */
    public static int familyWorkers(int neededWorkers, float familyShare) {
        return familyShare >= 1f ? neededWorkers : Math.round(neededWorkers * familyShare);
    }

    public static int neededWorkers(int activeWorkers, float effectiveLevel) {
        if (effectiveLevel <= 0f) {
            return activeWorkers;
        }
        return Math.min(activeWorkers, (int) Math.ceil(activeWorkers / (1f + RGO_MECHANIZATION_LABOUR_SAVING * effectiveLevel)));
    }
}

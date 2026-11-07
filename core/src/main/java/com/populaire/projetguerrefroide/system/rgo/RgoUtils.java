package com.populaire.projetguerrefroide.system.rgo;

import com.populaire.projetguerrefroide.component.CountryMarketView;

import static com.populaire.projetguerrefroide.simulation.Constants.NEEDS_SCALING_FACTOR;
import static com.populaire.projetguerrefroide.simulation.Constants.RURAL_WAGE_SHARE;

public class RgoUtils {

    public static float wageBill(CountryMarketView countryMarket, int workerPopTypeIndex, float minWageFactor, int workers) {
        float wagePerUnit = RURAL_WAGE_SHARE * (countryMarket.lifeCostsByPopType(workerPopTypeIndex) + 0.2f * countryMarket.everydayCostsByPopType(workerPopTypeIndex)) * (1f + minWageFactor);
        return wagePerUnit * workers / NEEDS_SCALING_FACTOR;
    }
}

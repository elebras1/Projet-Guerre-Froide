package com.populaire.projetguerrefroide.util;

import com.populaire.projetguerrefroide.component.CountryMarketView;

import static com.populaire.projetguerrefroide.util.Constants.NEEDS_SCALING_FACTOR;
import static com.populaire.projetguerrefroide.util.Constants.RURAL_WAGE_SHARE;

public class RgoUtils {

    public static float wageBill(CountryMarketView countryMarket, int workerPopTypeIndex, float minWageFactor, int workers) {
        float wagePerUnit = RURAL_WAGE_SHARE * (countryMarket.lifeCostsByPopType(workerPopTypeIndex) + 0.2f * countryMarket.everydayCostsByPopType(workerPopTypeIndex)) * (1f + minWageFactor);
        return wagePerUnit * workers / NEEDS_SCALING_FACTOR;
    }
}

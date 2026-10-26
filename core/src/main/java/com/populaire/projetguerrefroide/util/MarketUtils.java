package com.populaire.projetguerrefroide.util;

import com.populaire.projetguerrefroide.component.GlobalMarketView;

import static com.populaire.projetguerrefroide.util.Constants.RGO_STOCK_ADJUSTMENT_DAYS;
import static com.populaire.projetguerrefroide.util.Constants.STOCK_TARGET_DAYS;

public class MarketUtils {

    public static float clearingGap(GlobalMarketView globalMarket, int goodIndex) {
        float worldProduction = globalMarket.goodProductionAmounts(goodIndex);
        float clearingProduction = clearingProduction(globalMarket, goodIndex);
        return worldProduction > 0f ? clearingProduction / worldProduction - 1f : (clearingProduction > 0f ? 1f : 0f);
    }

    public static float clearingProduction(GlobalMarketView globalMarket, int goodIndex) {
        float worldProduction = globalMarket.goodProductionAmounts(goodIndex);
        float worldDemand = globalMarket.goodDemandAmounts(goodIndex);
        float inventory = globalMarket.goodStockAmounts(goodIndex) - worldProduction;
        return Math.max(0f, worldDemand - (inventory - STOCK_TARGET_DAYS * worldDemand) / RGO_STOCK_ADJUSTMENT_DAYS);
    }
}

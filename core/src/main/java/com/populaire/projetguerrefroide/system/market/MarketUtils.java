package com.populaire.projetguerrefroide.system.market;

import com.populaire.projetguerrefroide.component.GlobalMarketMutView;
import com.populaire.projetguerrefroide.component.GlobalMarketView;

import static com.populaire.projetguerrefroide.simulation.Constants.RGO_STOCK_ADJUSTMENT_DAYS;
import static com.populaire.projetguerrefroide.simulation.Constants.STOCK_TARGET_DAYS;

public class MarketUtils {

    public static float clearingGap(GlobalMarketView globalMarket, int goodIndex, float stockDecay) {
        float worldProduction = globalMarket.goodProductionAmounts(goodIndex);
        float clearingProduction = clearingProduction(globalMarket, goodIndex, stockDecay);
        return worldProduction > 0f ? clearingProduction / worldProduction - 1f : (clearingProduction > 0f ? 1f : 0f);
    }

    /**
     * World production that covers the demand, the daily loss of the stocks at their target and the return of the stocks
     * to that target (annex 12, decision 203): with stocks at {@code STOCK_TARGET_DAYS} of demand, a good that loses a share
     * d of its stocks a day needs 1 + STOCK_TARGET_DAYS x d times its demand (1.3 for fresh food, 1.075 for grain).
     */
    public static float clearingProduction(GlobalMarketView globalMarket, int goodIndex, float stockDecay) {
        float worldProduction = globalMarket.goodProductionAmounts(goodIndex);
        float worldDemand = globalMarket.goodDemandAmounts(goodIndex);
        float inventory = globalMarket.goodStockAmounts(goodIndex) - worldProduction;
        float target = STOCK_TARGET_DAYS * worldDemand;
        return Math.max(0f, worldDemand + stockDecay * target - (inventory - target) / RGO_STOCK_ADJUSTMENT_DAYS);
    }

    /**
     * Same, on the demand averaged over a year ({@code goodTrendDemands}): the base of lasting investments such as the
     * productivity gains of the RGO (decision 202), which must not follow a passing demand such as a refill of the
     * strategic stocks.
     */
    public static float trendClearingProduction(GlobalMarketView globalMarket, int goodIndex, float stockDecay) {
        float worldProduction = globalMarket.goodProductionAmounts(goodIndex);
        float worldDemand = globalMarket.goodTrendDemands(goodIndex);
        float inventory = globalMarket.goodStockAmounts(goodIndex) - worldProduction;
        float target = STOCK_TARGET_DAYS * worldDemand;
        return Math.max(0f, worldDemand + stockDecay * target - (inventory - target) / RGO_STOCK_ADJUSTMENT_DAYS);
    }

    /** One day of the yearly average of the world demand (exponential, a year of memory). */
    public static void updateTrendDemands(GlobalMarketMutView globalMarket) {
        for (int g = 0; g < globalMarket.goodDemandAmountsLength(); g++) {
            float demand = globalMarket.goodDemandAmounts(g);
            float trend = globalMarket.goodTrendDemands(g);
            globalMarket.goodTrendDemands(g, trend <= 0f ? demand : trend + (demand - trend) / 365f);
        }
    }
}

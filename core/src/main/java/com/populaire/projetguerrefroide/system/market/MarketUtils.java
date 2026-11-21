package com.populaire.projetguerrefroide.system.market;

import com.populaire.projetguerrefroide.component.CountryMarketView;
import com.populaire.projetguerrefroide.component.GlobalMarketMutView;
import com.populaire.projetguerrefroide.component.GlobalMarketView;

import static com.populaire.projetguerrefroide.simulation.Constants.RGO_STOCK_ADJUSTMENT_DAYS;
import static com.populaire.projetguerrefroide.simulation.Constants.STOCK_TARGET_DAYS;
import static com.populaire.projetguerrefroide.simulation.Constants.TARIFF_RAW;
import static com.populaire.projetguerrefroide.simulation.Constants.TRADE_COST;
import static com.populaire.projetguerrefroide.simulation.Constants.TRADE_ELASTICITY;

public class MarketUtils {

    public static float tariffRate(float floor, float legalRate, int category, float importShare) {
        if (category == TARIFF_RAW) {
            return floor;
        }
        return floor + Math.max(0f, legalRate - floor) * (1f - Math.clamp(importShare, 0f, 1f));
    }

    public static double tradeWeight(float stock, float price, float cost) {
        if (stock <= 0f || price <= 0f) {
            return 0.0;
        }
        return stock * Math.pow((double) price / cost, -TRADE_ELASTICITY);
    }

    public static float importShare(double ownWeight, double foreignWeight, float tariff) {
        if (foreignWeight <= 0.0) {
            return 0f;
        }
        double delivered = foreignWeight * Math.pow(TRADE_COST * (1.0 + tariff), -TRADE_ELASTICITY);
        return (float) (delivered / (ownWeight + delivered));
    }

    public static double foreignPrice(GlobalMarketView globalMarket, int goodIndex, double ownWeight, float price) {
        double foreignWeight = globalMarket.goodTradeWeights(goodIndex) - ownWeight;
        double foreignValue = globalMarket.goodTradeWeightedPrices(goodIndex) - ownWeight * price;
        if (foreignWeight <= 0.0 || foreignValue <= 0.0) {
            return price;
        }
        return foreignValue / foreignWeight;
    }

    public static float countryClearingGap(CountryMarketView countryMarket, int goodIndex) {
        float production = countryMarket.goodProducedAmounts(goodIndex);
        float demand = countryMarket.goodSalesAmounts(goodIndex);
        return production > 0f ? demand / production - 1f : (demand > 0f ? 1f : 0f);
    }

    public static float clearingGap(GlobalMarketView globalMarket, int goodIndex, float stockDecay) {
        float worldProduction = globalMarket.goodProductionAmounts(goodIndex);
        float clearingProduction = clearingProduction(globalMarket, goodIndex, stockDecay);
        return worldProduction > 0f ? clearingProduction / worldProduction - 1f : (clearingProduction > 0f ? 1f : 0f);
    }

    public static float clearingProduction(GlobalMarketView globalMarket, int goodIndex, float stockDecay) {
        float worldProduction = globalMarket.goodProductionAmounts(goodIndex);
        float worldDemand = globalMarket.goodDemandAmounts(goodIndex);
        float inventory = globalMarket.goodStockAmounts(goodIndex) - worldProduction;
        float target = STOCK_TARGET_DAYS * worldDemand;
        return Math.max(0f, worldDemand + stockDecay * target - (inventory - target) / RGO_STOCK_ADJUSTMENT_DAYS);
    }

    public static float trendClearingProduction(GlobalMarketView globalMarket, int goodIndex, float stockDecay) {
        float worldProduction = globalMarket.goodProductionAmounts(goodIndex);
        float worldDemand = globalMarket.goodTrendDemands(goodIndex);
        float inventory = globalMarket.goodStockAmounts(goodIndex) - worldProduction;
        float target = STOCK_TARGET_DAYS * worldDemand;
        return Math.max(0f, worldDemand + stockDecay * target - (inventory - target) / RGO_STOCK_ADJUSTMENT_DAYS);
    }

    public static void updateTrendDemands(GlobalMarketMutView globalMarket) {
        for (int g = 0; g < globalMarket.goodDemandAmountsLength(); g++) {
            float demand = globalMarket.goodDemandAmounts(g);
            float trend = globalMarket.goodTrendDemands(g);
            globalMarket.goodTrendDemands(g, trend <= 0f ? demand : trend + (demand - trend) / 365f);
        }
    }
}

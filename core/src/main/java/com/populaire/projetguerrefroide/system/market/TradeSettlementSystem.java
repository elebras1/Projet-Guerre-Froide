package com.populaire.projetguerrefroide.system.market;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;

import java.util.Arrays;

import static com.populaire.projetguerrefroide.simulation.Constants.TARIFF_CATEGORY_COUNT;
import static com.populaire.projetguerrefroide.simulation.Constants.TARIFF_TREND_DAYS;

public class TradeSettlementSystem {
    private final EcsConstants ecsConstants;
    private final double[] importedValues = new double[TARIFF_CATEGORY_COUNT];
    private final double[] consumedValues = new double[TARIFF_CATEGORY_COUNT];

    public TradeSettlementSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("TradeSettlementSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .with(CountryTradePolicy.class)
            .iter(this::settle);
    }

    private void settle(Iter iter) {
        EntityView globalMarket = iter.stage().obtainEntityView(this.ecsConstants.globalMarket());
        GlobalMarketView globalMarketData = globalMarket.getMutView(GlobalMarket.class);
        boolean[] localGoods = this.ecsConstants.localGoods();
        int[] tariffCategories = this.ecsConstants.goodTariffCategories();
        int day = iter.stage().obtainEntityView(this.ecsConstants.economyCalendar()).<EconomyCalendarView>getView(EconomyCalendar.class).day();
        float trendWeight = Math.min(TARIFF_TREND_DAYS, day + 1f);
        double[] importedValues = this.importedValues;
        double[] consumedValues = this.consumedValues;

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        Field<CountryTradePolicy> countryTradePolicyField = iter.field(CountryTradePolicy.class, 1);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
            CountryTradePolicyMutView countryTradePolicy = countryTradePolicyField.getMutView(i);

            Arrays.fill(importedValues, 0.0);
            Arrays.fill(consumedValues, 0.0);
            double tariffRevenue = 0.0;
            for (int g = 0; g < countryMarket.goodPricesLength(); g++) {
                float demand = countryMarket.goodDemandAmounts(g);
                float stock = countryMarket.goodStocks(g);
                float price = countryMarket.goodProducerPrices(g);
                float delivery = countryMarket.goodDeliveryRatios(g);
                double home = (double) demand * (1f - countryMarket.goodImportShares(g)) * delivery;
                if (localGoods[g]) {
                    countryMarket.goodStocks(g, (float) Math.max(0.0, stock - home));
                    countryMarket.goodConsumedAmounts(g, (float) home);
                    countryMarket.goodImportedAmounts(g, 0f);
                    countryMarket.goodExportedAmounts(g, 0f);
                    countryMarket.goodDemandSatisfactionRatios(g, demand > 0f ? (float) (home / demand) : 1f);
                    countryMarket.goodPrices(g, price);
                    continue;
                }
                double ownWeight = countryMarket.goodTradeWeights(g);
                double ownDemandShare = TradeDeliverySystem.ownForeignDemand(globalMarketData, countryMarket, g);
                double foreignAmount = 0.0;
                double foreignValue = 0.0;
                if (ownDemandShare > 0.0) {
                    double ownDelivered = ownWeight * delivery;
                    foreignAmount = ownDemandShare * Math.max(0.0, globalMarketData.goodDeliveredWeights(g) - ownDelivered);
                    foreignValue = ownDemandShare * Math.max(0.0, globalMarketData.goodDeliveredValues(g) - ownDelivered * price);
                }
                double exported = ownWeight > 0.0 ? ownWeight * delivery * Math.max(0.0, globalMarketData.goodForeignDemands(g) - ownDemandShare) : 0.0;
                double consumed = home + foreignAmount;
                float tariff = countryTradePolicy.categoryTariffRates(tariffCategories[g]);
                double duty = foreignValue * tariff;
                double exportValue = exported * price;
                double bill = home * price + foreignValue + duty;

                countryMarket.goodStocks(g, (float) Math.max(0.0, stock - home - exported));
                countryMarket.goodConsumedAmounts(g, (float) consumed);
                countryMarket.goodImportedAmounts(g, (float) foreignAmount);
                countryMarket.goodExportedAmounts(g, (float) exported);
                countryMarket.goodDemandSatisfactionRatios(g, demand > 0f ? (float) (consumed / demand) : 1f);
                countryMarket.goodPrices(g, consumed > 0.0 ? (float) (bill / consumed) : countryMarket.goodBudgetPrices(g));
                countryMarket.goodRevenues(g, countryMarket.goodRevenues(g) + exportValue - foreignValue - duty);

                tariffRevenue += duty;
                int category = tariffCategories[g];
                importedValues[category] += foreignValue;
                consumedValues[category] += home * price + foreignValue;
            }
            countryMarket.treasury(countryMarket.treasury() + tariffRevenue);
            countryMarket.tariffRevenue((float) tariffRevenue);

            for (int c = 0; c < TARIFF_CATEGORY_COUNT; c++) {
                float share = consumedValues[c] > 0.0 ? (float) (importedValues[c] / consumedValues[c]) : 0f;
                float previous = countryTradePolicy.importShareTrends(c);
                float trend = previous + (share - previous) / trendWeight;
                countryTradePolicy.importShareTrends(c, trend);
                countryTradePolicy.categoryTariffRates(c, MarketUtils.tariffRate(countryTradePolicy.tariffFloor(), countryTradePolicy.tariffRate(), c, trend));
            }
        }
    }
}

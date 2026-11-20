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

public class CountryPriceSystem {
    private final EcsConstants ecsConstants;
    private final double[] importedValues = new double[TARIFF_CATEGORY_COUNT];
    private final double[] consumedValues = new double[TARIFF_CATEGORY_COUNT];

    public CountryPriceSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("CountryPriceSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .with(CountryTradePolicy.class)
            .iter(this::price);
    }

    private void price(Iter iter) {
        EntityView globalMarket = iter.stage().obtainEntityView(this.ecsConstants.globalMarket());
        GlobalMarketView globalMarketData = globalMarket.getMutView(GlobalMarket.class);
        boolean[] localGoods = this.ecsConstants.localGoods();
        int[] tariffCategories = this.ecsConstants.goodTariffCategories();
        int day = iter.stage().obtainEntityView(this.ecsConstants.economyCalendar()).<EconomyCalendarView>getView(EconomyCalendar.class).day();
        float trendWeight = Math.min(TARIFF_TREND_DAYS, day + 1f);

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        Field<CountryTradePolicy> countryTradePolicyField = iter.field(CountryTradePolicy.class, 1);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
            CountryTradePolicyMutView countryTradePolicy = countryTradePolicyField.getMutView(i);

            Arrays.fill(this.importedValues, 0.0);
            Arrays.fill(this.consumedValues, 0.0);
            for (int g = 0; g < countryMarket.goodPricesLength(); g++) {
                if (localGoods[g]) {
                    continue;
                }
                int category = tariffCategories[g];
                float tariff = countryTradePolicy.categoryTariffRates(category);
                float consumed = countryMarket.goodConsumedAmounts(g);
                float imported = countryMarket.goodImportedAmounts(g);
                float importedShare = consumed > 0f ? imported / consumed : 0f;
                countryMarket.goodPrices(g, countryMarket.goodBudgetPrices(g) * ((1f + tariff * importedShare) / (1f + tariff)));
                float worldPrice = globalMarketData.goodPrices(g);
                this.importedValues[category] += (double) imported * worldPrice;
                this.consumedValues[category] += (double) consumed * worldPrice;
            }

            for (int c = 0; c < TARIFF_CATEGORY_COUNT; c++) {
                float share = this.consumedValues[c] > 0.0 ? (float) (this.importedValues[c] / this.consumedValues[c]) : 0f;
                float previous = countryTradePolicy.importShareTrends(c);
                float trend = previous + (share - previous) / trendWeight;
                countryTradePolicy.importShareTrends(c, trend);
                countryTradePolicy.categoryTariffRates(c, MarketUtils.tariffRate(countryTradePolicy.tariffFloor(), countryTradePolicy.tariffRate(), c, trend));
            }
        }
    }
}

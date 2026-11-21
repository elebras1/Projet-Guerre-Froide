package com.populaire.projetguerrefroide.system.state;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;
import com.populaire.projetguerrefroide.system.market.MarketUtils;

public class CountryBudgetPriceSystem {
    private final EcsConstants ecsConstants;

    public CountryBudgetPriceSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("CountryBudgetPriceSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .with(CountryTradePolicy.class)
            .multiThreaded()
            .iter(this::price);
    }

    private void price(Iter iter) {
        EntityView globalMarket = iter.stage().obtainEntityView(this.ecsConstants.globalMarket());
        GlobalMarketView globalMarketData = globalMarket.getMutView(GlobalMarket.class);

        boolean[] localGoods = this.ecsConstants.localGoods();
        int[] tariffCategories = this.ecsConstants.goodTariffCategories();
        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        Field<CountryTradePolicy> countryTradePolicyField = iter.field(CountryTradePolicy.class, 1);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
            CountryTradePolicyView countryTradePolicy = countryTradePolicyField.getMutView(i);

            for (int g = 0; g < countryMarket.goodBudgetPricesLength(); g++) {
                float producerPrice = countryMarket.goodProducerPrices(g);
                if (localGoods[g]) {
                    countryMarket.goodImportShares(g, 0f);
                    countryMarket.goodBudgetPrices(g, producerPrice);
                    continue;
                }
                double ownWeight = countryMarket.goodTradeWeights(g);
                double foreignWeight = globalMarketData.goodTradeWeights(g) - ownWeight;
                float tariff = countryTradePolicy.categoryTariffRates(tariffCategories[g]);
                float importShare = MarketUtils.importShare(ownWeight, foreignWeight, tariff);
                countryMarket.goodImportShares(g, importShare);
                if (importShare <= 0f) {
                    countryMarket.goodBudgetPrices(g, producerPrice);
                    continue;
                }
                double foreignPrice = MarketUtils.foreignPrice(globalMarketData, g, ownWeight, producerPrice);
                countryMarket.goodBudgetPrices(g, (float) ((1f - importShare) * producerPrice + importShare * (1f + tariff) * foreignPrice));
            }
        }
    }
}

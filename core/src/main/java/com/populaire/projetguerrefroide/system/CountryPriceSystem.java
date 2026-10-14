package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class CountryPriceSystem {

    public CountryPriceSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("CountryPriceSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .with(CountryTradePolicy.class)
            .iter(this::price);
    }

    private void price(Iter iter) {
        EntityView globalMarket = iter.world().obtainEntityView(iter.world().lookup("global_market"));
        GlobalMarketView globalMarketData = globalMarket.getMutView(GlobalMarket.class);

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        Field<CountryTradePolicy> countryTradePolicyField = iter.field(CountryTradePolicy.class, 1);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
            CountryTradePolicyView countryTradePolicy = countryTradePolicyField.getMutView(i);

            float tariffRate = countryTradePolicy.tariffRate();

            for (int g = 0; g < countryMarket.goodPricesLength(); g++) {
                float demand = countryMarket.goodDemandAmounts(g);
                float basePrice = globalMarketData.goodPrices(g);
                float importedFraction = Math.min(1.0f, countryMarket.goodImportedAmounts(g) / (demand + 0.001f));
                float referencePrice = basePrice * (1f + tariffRate * importedFraction);
                float scarcity = Math.max(0f, (demand - countryMarket.goodConsumedAmounts(g)) / (demand + 0.001f));
                float targetPrice = Math.max(0.001f, referencePrice * (1f + scarcity));
                float localPrice = countryMarket.goodPrices(g) * 0.95f + targetPrice * 0.05f;
                countryMarket.goodPrices(g, Math.max(0.001f, localPrice));
            }
        }
    }
}

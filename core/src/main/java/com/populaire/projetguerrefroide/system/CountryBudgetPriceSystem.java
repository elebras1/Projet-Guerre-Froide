package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;

import static com.populaire.projetguerrefroide.simulation.Constants.SHORTAGE_PRICE_PREMIUM;

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
        EntityView globalMarket = iter.world().obtainEntityView(this.ecsConstants.globalMarket());
        GlobalMarketView globalMarketData = globalMarket.getMutView(GlobalMarket.class);

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        Field<CountryTradePolicy> countryTradePolicyField = iter.field(CountryTradePolicy.class, 1);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
            CountryTradePolicyView countryTradePolicy = countryTradePolicyField.getMutView(i);

            float maximumTariff = 1f + Math.max(0f, countryTradePolicy.tariffRate());
            for (int g = 0; g < countryMarket.goodBudgetPricesLength(); g++) {
                float shortage = 1f - countryMarket.goodDemandSatisfactionRatios(g);
                countryMarket.goodBudgetPrices(g, globalMarketData.goodPrices(g) * (1f + SHORTAGE_PRICE_PREMIUM * shortage) * maximumTariff);
            }
        }
    }
}

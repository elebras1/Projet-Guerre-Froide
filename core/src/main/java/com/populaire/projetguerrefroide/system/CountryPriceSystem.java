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
            float effectiveTariffRate = tariffRate;
            if (tariffRate < 0f) {
                double subsidies = 0.0;
                for (int g = 0; g < countryMarket.goodPricesLength(); g++) {
                    subsidies += (double) countryMarket.goodImportedAmounts(g) * globalMarketData.goodPrices(g) * -tariffRate;
                }
                double affordableSubsidies = Math.min(subsidies, countryMarket.treasury());
                effectiveTariffRate = affordableSubsidies > 0.0 ? (float) (tariffRate * affordableSubsidies / subsidies) : 0f;
            }
            countryMarket.effectiveTariffRate(effectiveTariffRate);

            float maximumTariff = 1f + Math.max(0f, tariffRate);
            for (int g = 0; g < countryMarket.goodPricesLength(); g++) {
                float consumed = countryMarket.goodConsumedAmounts(g);
                float importedShare = consumed > 0f ? countryMarket.goodImportedAmounts(g) / consumed : 0f;
                countryMarket.goodPrices(g, countryMarket.goodBudgetPrices(g) * ((1f + effectiveTariffRate * importedShare) / maximumTariff));
            }
        }
    }
}

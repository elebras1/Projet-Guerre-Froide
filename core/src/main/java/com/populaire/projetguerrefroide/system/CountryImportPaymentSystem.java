package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class CountryImportPaymentSystem {

    public CountryImportPaymentSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("CountryImportPaymentSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .iter(this::pay);
    }

    private void pay(Iter iter) {
        EntityView globalMarket = iter.world().obtainEntityView(iter.world().lookup("global_market"));
        GlobalMarketMutView globalMarketData = globalMarket.getMutView(GlobalMarket.class);

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);

            float effectiveTariffRate = countryMarket.effectiveTariffRate();
            double tariffRevenue = 0.0;
            for (int g = 0; g < countryMarket.goodImportedAmountsLength(); g++) {
                double importValue = (double) countryMarket.goodImportedAmounts(g) * globalMarketData.goodPrices(g);
                if (importValue <= 0.0) {
                    continue;
                }
                double tariff = importValue * effectiveTariffRate;
                globalMarketData.goodTradeMoney(g, globalMarketData.goodTradeMoney(g) + importValue);
                countryMarket.goodRevenues(g, countryMarket.goodRevenues(g) - importValue - tariff);
                tariffRevenue += tariff;
            }
            countryMarket.treasury(countryMarket.treasury() + tariffRevenue);
            countryMarket.tariffRevenue((float) tariffRevenue);
        }
    }
}

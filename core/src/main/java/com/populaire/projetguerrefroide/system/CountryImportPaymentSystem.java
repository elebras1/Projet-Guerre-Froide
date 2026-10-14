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
            .with(CountryTradePolicy.class)
            .iter(this::pay);
    }

    private void pay(Iter iter) {
        EntityView globalMarket = iter.world().obtainEntityView(iter.world().lookup("global_market"));
        GlobalMarketMutView globalMarketData = globalMarket.getMutView(GlobalMarket.class);

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        Field<CountryTradePolicy> countryTradePolicyField = iter.field(CountryTradePolicy.class, 1);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
            CountryTradePolicyView countryTradePolicy = countryTradePolicyField.getMutView(i);

            float tariffRate = countryTradePolicy.tariffRate();
            double tariffRevenue = 0.0;
            for (int g = 0; g < countryMarket.goodImportedAmountsLength(); g++) {
                float importValue = countryMarket.goodImportedAmounts(g) * globalMarketData.goodPrices(g);
                if (importValue <= 0f) {
                    continue;
                }

                float tariff = importValue * tariffRate;
                double cash = Math.max(0.0, countryMarket.goodRevenues(g));
                double scale;
                if (tariff >= 0f) {
                    scale = Math.min(1.0, cash / (importValue + tariff));
                } else {
                    scale = Math.min(1.0, (cash + Math.min(-tariff, Math.max(0.0, countryMarket.treasury()))) / importValue);
                }

                double paidImport = importValue * scale;
                double taxPaid = Math.max(0f, tariff) * scale;
                double subsidyPaid = Math.min(Math.max(0f, -tariff) * scale, Math.max(0.0, countryMarket.treasury()));

                globalMarketData.goodTradeMoney(g, globalMarketData.goodTradeMoney(g) + paidImport);
                countryMarket.goodRevenues(g, countryMarket.goodRevenues(g) - paidImport - taxPaid + subsidyPaid);
                countryMarket.treasury(countryMarket.treasury() + taxPaid - subsidyPaid);
                tariffRevenue += taxPaid - subsidyPaid;
            }
            countryMarket.tariffRevenue((float) tariffRevenue);
        }
    }
}

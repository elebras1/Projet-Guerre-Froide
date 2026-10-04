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

            float importValue = 0f;
            for (int g = 0; g < countryMarket.goodImportedAmountsLength(); g++) {
                importValue += countryMarket.goodImportedAmounts(g) * globalMarketData.goodPrices(g);
            }
            if (importValue <= 0f) {
                continue;
            }

            float tariff = importValue * countryTradePolicy.tariffRate();
            float scale;
            if (tariff >= 0f) {
                float obligation = importValue + tariff;
                scale = obligation > 0f ? Math.min(1f, Math.max(0f, countryMarket.salesRevenue()) / obligation) : 0f;
            } else {
                float cash = Math.max(0f, countryMarket.salesRevenue()) + Math.min(-tariff, Math.max(0f, countryMarket.treasury()));
                scale = Math.min(1f, cash / importValue);
            }

            float paidImports = importValue * scale;
            float taxPaid = Math.max(0f, tariff) * scale;
            float subsidyPaid = Math.min(Math.max(0f, -tariff) * scale, Math.max(0f, countryMarket.treasury()));

            for (int g = 0; g < countryMarket.goodImportedAmountsLength(); g++) {
                float value = countryMarket.goodImportedAmounts(g) * globalMarketData.goodPrices(g) * scale;
                globalMarketData.goodTradeMoney(g, globalMarketData.goodTradeMoney(g) + value);
            }
            countryMarket.salesRevenue(countryMarket.salesRevenue() - paidImports - taxPaid + subsidyPaid);
            countryMarket.treasury(countryMarket.treasury() + taxPaid - subsidyPaid);
            countryMarket.tariffRevenue(taxPaid - subsidyPaid);
        }
    }
}

package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class CountryExportPayoutSystem {

    public CountryExportPayoutSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("CountryExportPayoutSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .with(CountryBudgetPolicy.class)
            .iter(this::pay);
    }

    private void pay(Iter iter) {
        EntityView globalMarket = iter.world().obtainEntityView(iter.world().lookup("global_market"));
        GlobalMarketView globalMarketData = globalMarket.getMutView(GlobalMarket.class);

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketView countryMarket = countryMarketField.getMutView(i);

            float payout = 0f;
            for (int g = 0; g < countryMarket.goodExportOffersLength(); g++) {
                float offers = countryMarket.goodExportOffers(g);
                float offerTotals = globalMarketData.goodOfferTotals(g);
                if (offers <= 0f || offerTotals <= 0f) {
                    continue;
                }
                payout += globalMarketData.goodTradeMoney(g) * offers / offerTotals;
            }
            countryMarket.salesRevenue(countryMarket.salesRevenue() + payout);
        }
    }
}

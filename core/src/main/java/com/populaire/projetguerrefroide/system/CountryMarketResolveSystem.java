package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class CountryMarketResolveSystem {

    public CountryMarketResolveSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("CountryMarketResolveSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .iter(this::resolve);
    }

    private void resolve(Iter iter) {
        EntityView globalMarket = iter.world().obtainEntityView(iter.world().lookup("global_market"));
        GlobalMarketMutView globalMarketData = globalMarket.getMutView(GlobalMarket.class);

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);

            for (int g = 0; g < countryMarket.goodPricesLength(); g++) {
                float stock = countryMarket.goodStocks(g);
                float demand = countryMarket.goodDemandAmounts(g);
                float imported = countryMarket.goodImportNeeds(g) * globalMarketData.goodTradeRatios(g);

                float consumedDomestic = Math.min(stock, demand);
                float consumedImport = Math.min(imported, demand - consumedDomestic);
                float consumed = consumedDomestic + consumedImport;

                float offerTotal = globalMarketData.goodOfferTotals(g);
                float exportRatio = offerTotal > 0f ? Math.min(1f, globalMarketData.goodNeedTotals(g) / offerTotal) : 0f;
                float exported = countryMarket.goodExportOffers(g) * exportRatio;

                countryMarket.goodDemandSatisfactionRatios(g, demand > 0f ? consumed / demand : 1f);

                countryMarket.goodStocks(g, stock - consumedDomestic - exported);
                countryMarket.goodConsumedAmounts(g, consumed);
                countryMarket.goodImportedAmounts(g, consumedImport);
                countryMarket.goodExportedAmounts(g, exported);
            }
        }
    }
}

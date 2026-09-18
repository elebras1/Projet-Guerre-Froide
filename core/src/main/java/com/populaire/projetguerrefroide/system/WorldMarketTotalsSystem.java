package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class WorldMarketTotalsSystem {

    public WorldMarketTotalsSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("WorldMarketTotalsSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .with(CountryProductionPolicy.class)
            .iter(this::accumulate);
    }

    private void accumulate(Iter iter) {
        EntityView globalMarket = iter.world().obtainEntityView(iter.world().lookup("global_market"));
        GlobalMarketView globalMarketData = globalMarket.getMutView(GlobalMarket.class);

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketView countryMarket = countryMarketField.getMutView(i);

            for (int g = 0; g < countryMarket.goodImportNeedsLength(); g++) {
                float demand = countryMarket.goodDemandAmounts(g);
                float domestic = countryMarket.goodAmountsPool(g);
                float stock = countryMarket.goodDrawingOnStockpiles(g) ? countryMarket.goodStockpiles(g) : 0f;

                float need = Math.max(0f, demand - domestic - stock);
                float offer = Math.max(0f, domestic - demand);

                countryMarket.goodImportNeeds(g, need);
                countryMarket.goodExportOffers(g, offer);
                globalMarketData.goodNeedTotals(g, globalMarketData.goodNeedTotals(g) + need);
                globalMarketData.goodOfferTotals(g, globalMarketData.goodOfferTotals(g) + offer);
            }
        }
    }
}

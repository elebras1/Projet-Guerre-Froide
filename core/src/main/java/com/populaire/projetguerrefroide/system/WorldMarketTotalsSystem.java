package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;

public class WorldMarketTotalsSystem {
    private final EcsConstants ecsConstants;

    public WorldMarketTotalsSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("WorldMarketTotalsSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .with(CountryProductionPolicy.class)
            .iter(this::accumulate);
    }

    private void accumulate(Iter iter) {
        EntityView globalMarket = iter.world().obtainEntityView(this.ecsConstants.globalMarket());
        GlobalMarketMutView globalMarketData = globalMarket.getMutView(GlobalMarket.class);

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);

            for (int g = 0; g < countryMarket.goodImportNeedsLength(); g++) {
                float demand = countryMarket.goodDemandAmounts(g);
                float stock = countryMarket.goodStocks(g);

                float need = Math.max(0f, demand - stock);
                float offer = Math.max(0f, stock - demand);

                countryMarket.goodImportNeeds(g, need);
                countryMarket.goodExportOffers(g, offer);
                globalMarketData.goodNeedTotals(g, globalMarketData.goodNeedTotals(g) + need);
                globalMarketData.goodOfferTotals(g, globalMarketData.goodOfferTotals(g) + offer);
            }
        }
    }
}

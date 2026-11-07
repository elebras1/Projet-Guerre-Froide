package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.CountryMarket;
import com.populaire.projetguerrefroide.component.CountryMarketView;
import com.populaire.projetguerrefroide.component.CountryMarketMutView;
import com.populaire.projetguerrefroide.component.GlobalMarket;
import com.populaire.projetguerrefroide.component.GlobalMarketMutView;
import com.populaire.projetguerrefroide.simulation.EcsConstants;

public class CountryProductionSpreadSystem {
    private final EcsConstants ecsConstants;

    public CountryProductionSpreadSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("CountryProductionSpreadSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .iter(this::spread);
    }

    private void spread(Iter iter) {
        EntityView globalMarket = iter.world().obtainEntityView(this.ecsConstants.globalMarket());
        GlobalMarketMutView globalMarketData = globalMarket.getMutView(GlobalMarket.class);

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        for(int i = 0; i < iter.count(); i++) {
            CountryMarketView countryMarket = countryMarketField.getMutView(i);

            for(int g = 0; g < countryMarket.goodProducedAmountsLength(); g++) {
                globalMarketData.goodProductionAmounts(g, globalMarketData.goodProductionAmounts(g) + countryMarket.goodProducedAmounts(g));
                globalMarketData.goodStockAmounts(g, globalMarketData.goodStockAmounts(g) + countryMarket.goodStocks(g));
            }
        }
    }
}

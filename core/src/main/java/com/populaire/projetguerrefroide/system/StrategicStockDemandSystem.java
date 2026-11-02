package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class StrategicStockDemandSystem {

    public StrategicStockDemandSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("StrategicStockDemandSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .with(CountryStrategicStock.class)
            .iter(this::demand);
    }

    private void demand(Iter iter) {
        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        Field<CountryStrategicStock> countryStrategicStockField = iter.field(CountryStrategicStock.class, 1);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
            CountryStrategicStockView stock = countryStrategicStockField.getMutView(i);
            for (int g = 0; g < stock.demandedAmountsLength(); g++) {
                float demanded = stock.demandedAmounts(g);
                if (demanded > 0f) {
                    countryMarket.goodDemandAmounts(g, countryMarket.goodDemandAmounts(g) + demanded);
                }
            }
        }
    }
}

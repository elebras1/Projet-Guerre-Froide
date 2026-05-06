package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.CountryMarket;
import com.populaire.projetguerrefroide.component.CountryMarketView;

public class StockpileDemandSystem {

    public StockpileDemandSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("StockpileDemandSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .iter(this::calculate);
    }

    private void calculate(Iter iter) {
        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        for(int i = 0; i < iter.count(); i++) {
            CountryMarketView countryMarket = countryMarketField.getMutView(i);
            for(int g = 0; g < countryMarket.goodStockpilesLength(); g++) {
                if(!countryMarket.goodDrawingOnStockpiles(g)) {
                    float demandStock = Math.max(0f, countryMarket.goodStockpileTargets(g) - countryMarket.goodStockpiles(g));
                    countryMarket.goodDemandAmounts(g, countryMarket.goodDemandAmounts(g) + demandStock);
                    countryMarket.goodStockpileDailyDeficits(g, demandStock);
                }
            }
        }
    }
}

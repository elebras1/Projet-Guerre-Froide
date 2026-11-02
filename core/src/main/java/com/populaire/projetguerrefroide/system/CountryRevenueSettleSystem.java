package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.CountryMarket;
import com.populaire.projetguerrefroide.component.CountryMarketMutView;
import com.populaire.projetguerrefroide.component.CountryStrategicStock;
import com.populaire.projetguerrefroide.component.CountryStrategicStockMutView;
import com.populaire.projetguerrefroide.component.CountryStrategicStockView;

public class CountryRevenueSettleSystem {

    public CountryRevenueSettleSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("CountryRevenueSettleSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .with(CountryStrategicStock.class)
            .iter(this::settle);
    }

    private void settle(Iter iter) {
        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        Field<CountryStrategicStock> countryStrategicStockField = iter.field(CountryStrategicStock.class, 1);
        for(int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
            CountryStrategicStockMutView stock = countryStrategicStockField.getMutView(i);
            double earned = 0.0;
            for(int g = 0; g < countryMarket.goodRevenuesLength(); g++) {
                float produced = countryMarket.goodProducedAmounts(g);
                if(produced <= 0f) {
                    countryMarket.treasury(countryMarket.treasury() + countryMarket.goodRevenues(g));
                } else if (stock.releasedAmounts(g) > 0f) {
                    double share = countryMarket.goodRevenues(g) * stock.releasedAmounts(g) / produced;
                    countryMarket.treasury(countryMarket.treasury() + share);
                    earned += share;
                }
                countryMarket.goodRevenues(g, 0.0);
            }
            stock.earnedToday(earned);
        }
    }
}

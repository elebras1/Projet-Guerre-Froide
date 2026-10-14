package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.CountryMarket;
import com.populaire.projetguerrefroide.component.CountryMarketMutView;

public class CountryRevenueSettleSystem {

    public CountryRevenueSettleSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("CountryRevenueSettleSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .multiThreaded()
            .iter(this::settle);
    }

    private void settle(Iter iter) {
        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        for(int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
            for(int g = 0; g < countryMarket.goodRevenuesLength(); g++) {
                if(countryMarket.goodProducedAmounts(g) <= 0f) {
                    countryMarket.treasury(countryMarket.treasury() + countryMarket.goodRevenues(g));
                }
                countryMarket.goodRevenues(g, countryMarket.goodPendingRevenues(g));
                countryMarket.goodPendingRevenues(g, 0.0);
            }
        }
    }
}

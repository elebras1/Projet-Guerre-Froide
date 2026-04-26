package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.CountryMarket;
import com.populaire.projetguerrefroide.component.CountryMarketView;

public class CountryMarketResetSystem {

    public CountryMarketResetSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("CountryMarketResetSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .iter(this::reset);
    }

    private void reset(Iter iter) {
        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        for(int i = 0; i < iter.count(); i++) {
            CountryMarketView countryMarket = countryMarketField.getMutView(i);

            for(int g = 0; g < countryMarket.realDemandLength(); g++) {
                countryMarket.realDemand(g, 0f);
                countryMarket.domesticMarketPool(g, 0f);
            }

        }
    }
}

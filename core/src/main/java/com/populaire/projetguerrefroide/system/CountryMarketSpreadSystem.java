package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.CountryMarket;
import com.populaire.projetguerrefroide.component.CountryMarketView;
import com.populaire.projetguerrefroide.component.GlobalMarket;
import com.populaire.projetguerrefroide.component.GlobalMarketView;

public class CountryMarketSpreadSystem {

    public CountryMarketSpreadSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("CountryMarketSpreadSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .iter(this::spread);
    }

    private void spread(Iter iter) {
        EntityView globalMarket = iter.world().obtainEntityView(iter.world().lookup("global_market"));
        GlobalMarketView globalMarketData = globalMarket.getMutView(GlobalMarket.class);

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        for(int i = 0; i < iter.count(); i++) {
            CountryMarketView countryMarket = countryMarketField.getMutView(i);

            for(int g = 0; g < countryMarket.goodAmountsPoolLength(); g++) {
                globalMarketData.goodLeftoverAmounts(g, globalMarketData.goodLeftoverAmounts(g) + countryMarket.goodAmountsPool(g));
                globalMarketData.goodDemandAmounts(g, globalMarketData.goodDemandAmounts(g) + countryMarket.goodDemandAmounts(g));
            }
        }
    }

}

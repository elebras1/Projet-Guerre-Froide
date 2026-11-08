package com.populaire.projetguerrefroide.system.market;

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

public class CountryMarketSpreadSystem {
    private final EcsConstants ecsConstants;

    public CountryMarketSpreadSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("CountryMarketSpreadSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .iter(this::spread);
    }

    private void spread(Iter iter) {
        EntityView globalMarket = iter.stage().obtainEntityView(this.ecsConstants.globalMarket());
        GlobalMarketMutView globalMarketData = globalMarket.getMutView(GlobalMarket.class);

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        for(int i = 0; i < iter.count(); i++) {
            CountryMarketView countryMarket = countryMarketField.getMutView(i);

            for(int g = 0; g < countryMarket.goodDemandAmountsLength(); g++) {
                globalMarketData.goodDemandAmounts(g, globalMarketData.goodDemandAmounts(g) + countryMarket.goodDemandAmounts(g));
            }
        }
    }

}

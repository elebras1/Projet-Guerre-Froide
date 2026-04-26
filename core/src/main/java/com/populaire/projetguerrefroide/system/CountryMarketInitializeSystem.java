package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class CountryMarketInitializeSystem {

    public CountryMarketInitializeSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("CountryMarketInitializeSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .iter(this::initialize);
    }

    private void initialize(Iter iter) {
        long globalGoodId = iter.world().lookup("global_good");
        EntityView globalGood = iter.world().obtainEntityView(globalGoodId);
        GlobalGoodView globalGoodData = globalGood.getMutView(GlobalGood.class);

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        for(int i = 0; i < iter.count(); i++) {
            CountryMarketView countryMarket = countryMarketField.getMutView(i);

            for(int g = 0; g < globalGoodData.goodIdsLength(); g++) {
                EntityView good = iter.world().obtainEntityView(globalGoodData.goodIds(g));
                GoodView goodData = good.getMutView(Good.class);
                countryMarket.effectiveGoodPrices(g, goodData.cost());
                countryMarket.demandSatisfaction(g, 1f);
            }
            countryMarket.spendingRatio(1f);
        }
    }
}

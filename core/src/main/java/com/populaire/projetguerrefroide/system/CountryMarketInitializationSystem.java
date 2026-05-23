package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class CountryMarketInitializationSystem {

    public CountryMarketInitializationSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("countryMarketInitializationSystem")
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
                countryMarket.goodPrices(g, goodData.cost());
                countryMarket.goodDemandSatisfactionRatios(g, 1f);
            }
            countryMarket.spendingRatio(1f);
        }
    }
}

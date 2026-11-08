package com.populaire.projetguerrefroide.system.market;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;

public class CountryMarketInitializationSystem {
    private final EcsConstants ecsConstants;

    public CountryMarketInitializationSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("countryMarketInitializationSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .multiThreaded()
            .iter(this::initialize);
    }

    private void initialize(Iter iter) {
        long globalGoodId = this.ecsConstants.globalGood();
        EntityView globalGood = iter.stage().obtainEntityView(globalGoodId);
        GlobalGoodView globalGoodData = globalGood.getMutView(GlobalGood.class);

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        for(int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);

            for(int g = 0; g < globalGoodData.goodIdsLength(); g++) {
                EntityView good = iter.stage().obtainEntityView(globalGoodData.goodIds(g));
                GoodView goodData = good.getMutView(Good.class);
                countryMarket.goodPrices(g, goodData.cost());
                countryMarket.goodDemandSatisfactionRatios(g, 1f);
            }
        }
    }
}

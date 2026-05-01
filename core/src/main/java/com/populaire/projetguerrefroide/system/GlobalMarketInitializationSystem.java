package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class GlobalMarketInitializationSystem {

    public GlobalMarketInitializationSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("GlobalMarketInitializationSystem")
            .kind(phaseId)
            .with(GlobalMarket.class)
            .iter(this::initialize);
    }

    private void initialize(Iter iter) {
        EntityView globalGood = iter.world().obtainEntityView(iter.world().lookup("global_good"));
        GlobalGoodView globalGoodData = globalGood.getMutView(GlobalGood.class);

        Field<GlobalMarket> globalMarketField = iter.field(GlobalMarket.class, 0);
        for(int i = 0; i < iter.count(); i++) {
            GlobalMarketView globalMarket = globalMarketField.getMutView(i);

            for(int g = 0; g < globalMarket.goodAmountsPoolLength(); g++) {
                EntityView good = iter.world().obtainEntityView(globalGoodData.goodIds(g));
                GoodView goodData = good.getMutView(Good.class);
                globalMarket.goodPrices(g, goodData.cost());
            }
        }
    }
}

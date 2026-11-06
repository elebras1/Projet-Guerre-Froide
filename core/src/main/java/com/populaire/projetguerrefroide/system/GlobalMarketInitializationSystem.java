package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;

public class GlobalMarketInitializationSystem {
    private final EcsConstants ecsConstants;

    public GlobalMarketInitializationSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("GlobalMarketInitializationSystem")
            .kind(phaseId)
            .with(GlobalMarket.class)
            .multiThreaded()
            .iter(this::initialize);
    }

    private void initialize(Iter iter) {
        EntityView globalGood = iter.world().obtainEntityView(this.ecsConstants.globalGood());
        GlobalGoodView globalGoodData = globalGood.getMutView(GlobalGood.class);

        Field<GlobalMarket> globalMarketField = iter.field(GlobalMarket.class, 0);
        for(int i = 0; i < iter.count(); i++) {
            GlobalMarketMutView globalMarket = globalMarketField.getMutView(i);

            for(int g = 0; g < globalMarket.goodPricesLength(); g++) {
                EntityView good = iter.world().obtainEntityView(globalGoodData.goodIds(g));
                GoodView goodData = good.getMutView(Good.class);
                globalMarket.goodPrices(g, goodData.cost());
                globalMarket.goodAvailabilities(g, true);
            }
        }
    }
}

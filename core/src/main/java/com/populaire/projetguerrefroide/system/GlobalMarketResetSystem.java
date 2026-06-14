package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.GlobalMarket;
import com.populaire.projetguerrefroide.component.GlobalMarketView;

public class GlobalMarketResetSystem {

    public GlobalMarketResetSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("GlobalMarketResetSystem")
            .kind(phaseId)
            .with(GlobalMarket.class)
            .multiThreaded()
            .iter(this::reset);
    }

    private void reset(Iter iter) {
        Field<GlobalMarket> globalMarketField = iter.field(GlobalMarket.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            GlobalMarketView globalMarket = globalMarketField.getMutView(i);
            for (int g = 0; g < globalMarket.goodProductionAmountsLength(); g++) {
                globalMarket.goodProductionAmounts(g, 0f);
                globalMarket.goodDemandAmounts(g, 0f);
                globalMarket.goodLeftoverAmounts(g, 0f);
            }
        }
    }
}

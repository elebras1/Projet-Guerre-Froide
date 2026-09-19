package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.GlobalMarket;
import com.populaire.projetguerrefroide.component.GlobalMarketView;

public class GlobalTradeRatioSystem {

    public GlobalTradeRatioSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("GlobalTradeRatioSystem")
            .kind(phaseId)
            .with(GlobalMarket.class)
            .multiThreaded()
            .iter(this::compute);
    }

    private void compute(Iter iter) {
        Field<GlobalMarket> globalMarketField = iter.field(GlobalMarket.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            GlobalMarketView globalMarket = globalMarketField.getMutView(i);

            for (int g = 0; g < globalMarket.goodTradeRatiosLength(); g++) {
                float available = globalMarket.goodAmountsPool(g) + globalMarket.goodOfferTotals(g);
                float needs = globalMarket.goodNeedTotals(g);

                globalMarket.goodTradeRatios(g, needs > 0f ? Math.min(1f, available / needs) : 0f);
            }
        }
    }
}

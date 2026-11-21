package com.populaire.projetguerrefroide.system.market;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.GlobalMarket;
import com.populaire.projetguerrefroide.component.GlobalMarketMutView;

public class GlobalMarketResolveSystem {

    public GlobalMarketResolveSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("GlobalMarketResolveSystem")
            .kind(phaseId)
            .with(GlobalMarket.class)
            .multiThreaded()
            .iter(this::resolve);
    }

    public void resolve(Iter iter) {
        Field<GlobalMarket> marketField = iter.field(GlobalMarket.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            GlobalMarketMutView gm = marketField.getMutView(i);
            for (int g = 0; g < gm.goodPricesLength(); g++) {
                gm.goodAvailabilities(g, gm.goodStockAmounts(g) > 0f);
            }
        }
    }
}

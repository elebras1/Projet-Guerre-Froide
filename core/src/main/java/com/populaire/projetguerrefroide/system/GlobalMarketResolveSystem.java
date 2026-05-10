package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.GlobalMarket;
import com.populaire.projetguerrefroide.component.GlobalMarketView;

public class GlobalMarketResolveSystem {

    public GlobalMarketResolveSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("GlobalMarketResolveSystem")
            .kind(phaseId)
            .with(GlobalMarket.class)
            .iter(this::resolve);
    }

    public void resolve(Iter iter) {
        Field<GlobalMarket> globalMarketField = iter.field(GlobalMarket.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            GlobalMarketView globalMarket = globalMarketField.getMutView(i);
            for (int g = 0; g < globalMarket.goodAmountsPoolLength(); g++) {
                float pool = globalMarket.goodAmountsPool(g);
                float supply = globalMarket.goodProductionAmounts(g) + pool * 0.5f;
                float demand = globalMarket.goodDemandAmounts(g);
                float ratio = (demand + 0.001f) / (supply + 0.001f);
                float priceAdjustment = ratio - 1.0f;

                float oldPrice = globalMarket.goodPrices(g);
                float inertia = 0.05f;
                float newPrice = oldPrice * (1.0f + inertia * priceAdjustment);
                globalMarket.goodPrices(g, Math.max(0.001f, newPrice));

                globalMarket.goodAmountsPool(g, pool * 0.5f);
            }
        }
    }
}

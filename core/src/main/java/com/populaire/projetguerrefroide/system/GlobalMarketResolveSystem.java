package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
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
        Field<GlobalMarket> marketField = iter.field(GlobalMarket.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            GlobalMarketView gm = marketField.getMutView(i);

            for (int g = 0; g < gm.goodAmountsPoolLength(); g++) {
                float oldPool = gm.goodAmountsPool(g);
                float decayedPool = oldPool * 0.5f;

                float leftover = gm.goodLeftoverAmounts(g);
                float newPool = decayedPool + leftover;

                float supply = gm.goodProductionAmounts(g) + newPool / 12.0f;
                float demand = gm.goodDemandAmounts(g);

                float oversupply = Math.clamp(((supply + 2.0f) / (demand + 2.0f) - 1.0f) * 10.0f, 0.0f, 10.0f);
                float overdemand = Math.clamp(((demand + 2.0f) / (supply + 2.0f) - 1.0f) * 10.0f, 0.0f, 10.0f);

                float speedModifier = overdemand - oversupply;
                if (Math.abs(overdemand - oversupply) < 1.0f) {
                    speedModifier = speedModifier * speedModifier * speedModifier;
                }

                float currentPrice = gm.goodPrices(g);
                float priceSpeed = 0.00005f * Math.max(0.1f, currentPrice) * speedModifier;
                float newPrice = currentPrice + priceSpeed;
                newPrice = Math.max(0.001f, newPrice);

                gm.goodPrices(g, newPrice);
                gm.goodAmountsPool(g, newPool);
            }
        }
    }
}

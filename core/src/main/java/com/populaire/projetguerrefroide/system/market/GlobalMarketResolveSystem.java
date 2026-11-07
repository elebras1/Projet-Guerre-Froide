package com.populaire.projetguerrefroide.system.market;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;

import static com.populaire.projetguerrefroide.simulation.Constants.*;

public class GlobalMarketResolveSystem {
    private final EcsConstants ecsConstants;

    public GlobalMarketResolveSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("GlobalMarketResolveSystem")
            .kind(phaseId)
            .with(GlobalMarket.class)
            .multiThreaded()
            .iter(this::resolve);
    }

    public void resolve(Iter iter) {
        EntityView globalGood = iter.world().obtainEntityView(this.ecsConstants.globalGood());
        GlobalGoodView globalGoodData = globalGood.getMutView(GlobalGood.class);
        int serviceGoodStartIndex = globalGoodData.serviceGoodStartIndex();
        int serviceGoodEndIndex = globalGoodData.serviceGoodEndIndex();

        Field<GlobalMarket> marketField = iter.field(GlobalMarket.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            GlobalMarketMutView gm = marketField.getMutView(i);

            for (int g = 0; g < gm.goodPricesLength(); g++) {
                float demand = gm.goodDemandAmounts(g);
                float production = gm.goodProductionAmounts(g);
                float offer = production;
                if (g < serviceGoodStartIndex || g >= serviceGoodEndIndex) {
                    float inventory = gm.goodStockAmounts(g) - production;
                    offer += (inventory - STOCK_TARGET_DAYS * demand) / STOCK_ADJUSTMENT_DAYS;
                }

                float scale = Math.max(demand, offer);
                float excessDemand = scale > 0f ? Math.clamp((demand - offer) / scale, -1f, 1f) : 0f;

                EntityView good = iter.world().obtainEntityView(globalGoodData.goodIds(g));
                GoodView goodData = good.getMutView(Good.class);
                float cost = goodData.cost();
                float price = gm.goodPrices(g) * (1f + PRICE_ADJUSTMENT_RATE * excessDemand);
                gm.goodPrices(g, Math.clamp(price, MINIMUM_PRICE_RATIO * cost, MAXIMUM_PRICE_RATIO * cost));
                gm.goodAvailabilities(g, gm.goodStockAmounts(g) > 0f);
            }
        }
    }
}

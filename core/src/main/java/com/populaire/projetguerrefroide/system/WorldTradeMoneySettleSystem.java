package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.GlobalMarket;
import com.populaire.projetguerrefroide.component.GlobalMarketMutView;

public class WorldTradeMoneySettleSystem {

    public WorldTradeMoneySettleSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("WorldTradeMoneySettleSystem")
            .kind(phaseId)
            .with(GlobalMarket.class)
            .multiThreaded()
            .iter(this::settle);
    }

    private void settle(Iter iter) {
        Field<GlobalMarket> globalMarketField = iter.field(GlobalMarket.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            GlobalMarketMutView globalMarket = globalMarketField.getMutView(i);

            for (int g = 0; g < globalMarket.goodTradeMoneyLength(); g++) {
                if (globalMarket.goodOfferTotals(g) > 0f) {
                    globalMarket.goodTradeMoney(g, 0f);
                }
            }
        }
    }
}

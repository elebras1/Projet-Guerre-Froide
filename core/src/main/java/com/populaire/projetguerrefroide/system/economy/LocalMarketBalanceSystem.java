package com.populaire.projetguerrefroide.system.economy;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.util.FlecsConstants;
import com.populaire.projetguerrefroide.component.*;

public class LocalMarketBalanceSystem {

    public LocalMarketBalanceSystem(World ecsWorld) {
        ecsWorld.system("LocalMarketBalanceSystem")
            .kind(FlecsConstants.EcsOnUpdate)
            .with(LocalMarket.class)
            .with(LocalMarketState.class)
            .iter(this::balance);
    }

    private void balance(Iter iter) {
        Field<LocalMarket> localMarketField = iter.field(LocalMarket.class, 0);
        Field<LocalMarketState> stateField = iter.field(LocalMarketState.class, 1);
        for (int i = 0; i < iter.count(); i++) {
            LocalMarketView localMarket = localMarketField.getMutView(i);
            LocalMarketStateView localMarketState = stateField.getMutView(i);
            for (int goodIndex = 0; goodIndex < localMarket.goodProductionsLength(); goodIndex++) {
                float supply = localMarket.goodProductions(goodIndex);
                float demand = localMarket.goodConsumptions(goodIndex);
                float multiplier;
                if (demand <= 0f) {
                    multiplier = 1f;
                } else if (supply >= demand) {
                    multiplier = 1f;
                } else {
                    multiplier = supply / demand;
                }
                localMarketState.throughputMultipliers(goodIndex, multiplier);
            }
        }
    }
}


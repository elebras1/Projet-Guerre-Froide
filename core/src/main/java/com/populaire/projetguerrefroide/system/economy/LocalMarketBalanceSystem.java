package com.populaire.projetguerrefroide.system.economy;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.util.FlecsConstants;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.service.GameContext;

public class LocalMarketBalanceSystem {

    public LocalMarketBalanceSystem(World ecsWorld, GameContext gameContext) {
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

                float production = localMarket.goodProductions(goodIndex);
                float consumption = localMarket.goodConsumptions(goodIndex);

                float multiplier;
                if (consumption <= 0f) {
                    multiplier = 1f;
                } else if (production >= consumption) {
                    multiplier = 1f;
                } else {
                    multiplier = production / consumption;
                }
                localMarketState.throughputMultipliers(goodIndex, multiplier);
            }
        }
    }
}


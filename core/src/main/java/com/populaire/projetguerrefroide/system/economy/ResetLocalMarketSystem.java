package com.populaire.projetguerrefroide.system.economy;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.util.FlecsConstants;
import com.populaire.projetguerrefroide.component.LocalMarket;
import com.populaire.projetguerrefroide.component.LocalMarketView;

public class ResetLocalMarketSystem {

    public ResetLocalMarketSystem(World ecsWorld) {
        ecsWorld.system("ResetLocalMarketSystem")
            .kind(FlecsConstants.EcsPreUpdate)
            .with(LocalMarket.class)
            .iter(this::reset);
    }

    private void reset(Iter iter) {
        Field<LocalMarket> localMarketField = iter.field(LocalMarket.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            LocalMarketView localMarket = localMarketField.getMutView(i);
            for (int goodIndex = 0; goodIndex < localMarket.goodProductionsLength(); goodIndex++) {
                localMarket.goodProductions(goodIndex, 0f);
            }
            for (int goodIndex = 0; goodIndex < localMarket.goodConsumptionsLength(); goodIndex++) {
                localMarket.goodConsumptions(goodIndex, 0f);
            }
        }
    }
}


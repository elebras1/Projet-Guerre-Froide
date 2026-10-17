package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.GlobalMarket;
import com.populaire.projetguerrefroide.component.GlobalMarketMutView;

public class GoodAvailabilityInitializationSystem {

    public GoodAvailabilityInitializationSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("GoodAvailabilityInitializationSystem")
            .kind(phaseId)
            .with(GlobalMarket.class)
            .iter(this::initialize);
    }

    private void initialize(Iter iter) {
        Field<GlobalMarket> globalMarketField = iter.field(GlobalMarket.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            GlobalMarketMutView globalMarket = globalMarketField.getMutView(i);
            for (int g = 0; g < globalMarket.goodAvailabilitiesLength(); g++) {
                globalMarket.goodAvailabilities(g, globalMarket.goodProductionAmounts(g) > 0f);
            }
        }
    }
}

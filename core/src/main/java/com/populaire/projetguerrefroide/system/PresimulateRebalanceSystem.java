package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class PresimulateRebalanceSystem {

    public PresimulateRebalanceSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("PresimulateRebalanceSystem")
            .kind(phaseId)
            .with(ResourceGathering.class)
            .iter(this::rebalance);
    }

    private void rebalance(Iter iter) {
        EntityView globalMarket = iter.world().obtainEntityView(iter.world().lookup("global_market"));
        GlobalMarketView globalMarketData = globalMarket.getMutView(GlobalMarket.class);

        Field<ResourceGathering> resourceGatheringField = iter.field(ResourceGathering.class, 0);
        for(int i = 0; i < iter.count(); i++) {
            ResourceGatheringView resourceGathering = resourceGatheringField.getMutView(i);

            int goodIndex = resourceGathering.goodIndex();
            float production = globalMarketData.goodProductionAmounts(goodIndex);
            float demand = globalMarketData.goodDemandAmounts(goodIndex);

            if(production > 1f && demand > production) {
                float multiplier = demand / production;
                resourceGathering.goodAmount(resourceGathering.goodAmount() * multiplier);
            }
        }
    }
}

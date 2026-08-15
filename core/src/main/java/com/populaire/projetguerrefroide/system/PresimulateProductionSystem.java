package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class PresimulateProductionSystem {

    public PresimulateProductionSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("PresimulateProductionSystem")
            .kind(phaseId)
            .with(ResourceGathering.class)
            .iter(this::accumulate);
    }

    private void accumulate(Iter iter) {
        EntityView globalMarket = iter.world().obtainEntityView(iter.world().lookup("global_market"));
        GlobalMarketView globalMarketData = globalMarket.getMutView(GlobalMarket.class);

        Field<ResourceGathering> resourceGatheringField = iter.field(ResourceGathering.class, 0);
        for(int i = 0; i < iter.count(); i++) {
            ResourceGatheringView resourceGathering = resourceGatheringField.getMutView(i);

            int goodIndex = resourceGathering.goodIndex();
            float production = (float) resourceGathering.size() * resourceGathering.goodAmount();
            globalMarketData.goodProductionAmounts(goodIndex, globalMarketData.goodProductionAmounts(goodIndex) + production);
        }
    }
}

package com.populaire.projetguerrefroide.system.economy;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.util.FlecsConstants;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.service.EconomyRuntime;
import com.populaire.projetguerrefroide.service.GameContext;

public class ResourceGatheringOperationProduceSystem {
    private final EconomyRuntime economyRuntime;

    public ResourceGatheringOperationProduceSystem(World ecsWorld, GameContext gameContext) {
        this.economyRuntime = gameContext.getEconomyRuntime();
        ecsWorld.system("RGOProduceSystem")
            .kind(FlecsConstants.EcsOnUpdate)
            .with(Province.class)
            .with(ResourceGathering.class)
            .with(GeoHierarchy.class)
            .iter(this::produce);
    }

    public void produce(Iter iter) {
        Field<ResourceGathering> resourceGatheringField = iter.field(ResourceGathering.class, 1);
        Field<GeoHierarchy> geoHierarchyField = iter.field(GeoHierarchy.class, 2);
        for (int i = 0; i < iter.count(); i++) {
            ResourceGatheringView resourceGatheringView = resourceGatheringField.getMutView(i);
            GeoHierarchyView geoHierarchyView = geoHierarchyField.getMutView(i);

            int goodIndex = resourceGatheringView.goodIndex();
            int resourceGoodSize = resourceGatheringView.size();
            int workforce = resourceGatheringView.workforce();

            float baseProduction = resourceGoodSize * resourceGatheringView.goodValue();

            int totalWorkers = 0;
            for (int j = 0; j < resourceGatheringView.hiredWorkersLength(); j++) {
                int hiredWorker = resourceGatheringView.hiredWorkers(j);
                totalWorkers += hiredWorker;
            }

            int maxWorkers = resourceGoodSize * workforce;
            float throughput = maxWorkers > 0 ? (float) totalWorkers / maxWorkers : 0f;
            float production = baseProduction * throughput;

            resourceGatheringView.production(production);
            this.economyRuntime.addMarketGoodProductions(geoHierarchyView.localMarketIndex(), goodIndex, production);
        }
    }
}

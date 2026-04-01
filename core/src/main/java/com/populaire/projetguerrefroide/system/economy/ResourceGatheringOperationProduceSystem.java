package com.populaire.projetguerrefroide.system.economy;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.util.FlecsConstants;
import com.populaire.projetguerrefroide.component.*;

public class ResourceGatheringOperationProduceSystem {
    private final World ecsWorld;

    public ResourceGatheringOperationProduceSystem(World ecsWorld) {
        this.ecsWorld = ecsWorld;
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
            ResourceGatheringView resourceGathering = resourceGatheringField.getMutView(i);
            GeoHierarchyView geoHierarchy = geoHierarchyField.getMutView(i);

            int goodIndex = resourceGathering.goodIndex();
            int resourceGoodSize = resourceGathering.size();
            int workforce = resourceGathering.workforce();

            float baseProduction = resourceGoodSize * resourceGathering.goodValue();

            int totalWorkers = 0;
            for (int j = 0; j < resourceGathering.hiredWorkersLength(); j++) {
                int hiredWorker = resourceGathering.hiredWorkers(j);
                totalWorkers += hiredWorker;
            }

            int maxWorkers = resourceGoodSize * workforce;
            float throughput = maxWorkers > 0 ? (float) totalWorkers / maxWorkers : 0f;
            float production = baseProduction * throughput;

            resourceGathering.production(production);

            EntityView localMarket = this.ecsWorld.obtainEntityView(geoHierarchy.localMarketId());
            LocalMarketView localMarketData = localMarket.getMutView(LocalMarket.class);
            localMarketData.goodProductions(goodIndex, localMarketData.goodProductions(goodIndex) + production);
        }
    }
}

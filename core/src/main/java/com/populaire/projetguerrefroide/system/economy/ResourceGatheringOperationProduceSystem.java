package com.populaire.projetguerrefroide.system.economy;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.util.FlecsConstants;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.service.EconomyService;

public class ResourceGatheringOperationProduceSystem {
    private final World ecsWorld;
    private final EconomyService economyService;

    public ResourceGatheringOperationProduceSystem(World ecsWorld, EconomyService economyService) {
        this.ecsWorld = ecsWorld;
        this.economyService = economyService;
        ecsWorld.system("RGOProduceSystem").kind(FlecsConstants.EcsOnUpdate).with(Province.class).with(ResourceGathering.class).multiThreaded().iter(this::process);
    }

    public void process(Iter iter) {
        Field<ResourceGathering> resourceGatheringField = iter.field(ResourceGathering.class, 1);
        for (int i = 0; i < iter.count(); i++) {
            ResourceGatheringView resourceGatheringView = resourceGatheringField.getMutView(i);

            long resourceGoodId = resourceGatheringView.goodId();
            int resourceGoodSize = resourceGatheringView.size();

            EntityView resourceGoodView = this.ecsWorld.obtainEntityView(resourceGoodId);
            GoodView good = resourceGoodView.getMutView(Good.class);

            float baseProduction = resourceGoodSize * good.value();

            int totalWorkers = 0;
            for (int j = 0; j < resourceGatheringView.hiredWorkersLength(); j++) {
                int hiredWorker = resourceGatheringView.hiredWorkers(j);
                totalWorkers += hiredWorker;
            }

            int maxWorkers = this.economyService.getMaxWorkers(this.ecsWorld, resourceGoodId, resourceGoodSize);
            float throughput = maxWorkers > 0 ? (float) totalWorkers / maxWorkers : 0f;
            float production = baseProduction * throughput;

            resourceGatheringView.production(production);
        }
    }
}

package com.populaire.projetguerrefroide.system.economy;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.github.elebras1.flecs.util.FlecsConstants;
import com.populaire.projetguerrefroide.component.Good;
import com.populaire.projetguerrefroide.component.Province;
import com.populaire.projetguerrefroide.component.ResourceGathering;
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
        for (int i = 0; i < iter.count(); i++) {
            long provinceEntityId = iter.entity(i);
            Entity provinceEntity = ecsWorld.obtainEntity(provinceEntityId);

            long resourceGoodId = iter.fieldLong(ResourceGathering.class, 1, "goodId", i);

            ResourceGathering currentState = provinceEntity.get(ResourceGathering.class);
            int resourceGoodSize = currentState.size();

            Entity resourceGoodEntity = ecsWorld.obtainEntity(resourceGoodId);
            Good good = resourceGoodEntity.get(Good.class);

            float baseProduction = resourceGoodSize * good.value();

            int totalWorkers = 0;
            int[] hiredWorkers = currentState.hiredWorkers();
            for (int hiredWorker : hiredWorkers) {
                totalWorkers += hiredWorker;
            }

            int maxWorkers = this.economyService.getMaxWorkers(ecsWorld, resourceGoodId, resourceGoodSize);
            float throughput = maxWorkers > 0 ? (float) totalWorkers / maxWorkers : 0f;
            float production = baseProduction * throughput;

            iter.setFieldFloat(ResourceGathering.class, 1, "production", i, production);
        }
    }
}

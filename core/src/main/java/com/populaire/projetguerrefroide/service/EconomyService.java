package com.populaire.projetguerrefroide.service;

import com.populaire.projetguerrefroide.repository.QueryRepository;
import com.populaire.projetguerrefroide.system.economy.ResourceGatheringOperationHireSystem;
import com.populaire.projetguerrefroide.system.economy.ResourceGatheringOperationProduceSystem;
import com.populaire.projetguerrefroide.system.economy.ResourceGatheringOperationSizeSystem;

public class EconomyService {
    private final GameContext gameContext;
    private final QueryRepository queryRepository;
    private final BuildingService buildingService;
    private final ResourceGatheringOperationSizeSystem rgoSizeSystem;
    private final ResourceGatheringOperationHireSystem rgoHireSystem;
    private final ResourceGatheringOperationProduceSystem rgoProduceSystem;

    public EconomyService(GameContext gameContext, QueryRepository queryRepository, BuildingService buildingService) {
        this.gameContext = gameContext;
        this.queryRepository = queryRepository;
        this.buildingService = buildingService;
        this.rgoSizeSystem = new ResourceGatheringOperationSizeSystem(this.gameContext.getEcsWorld());
        this.rgoHireSystem = new ResourceGatheringOperationHireSystem(this.gameContext.getEcsWorld(), buildingService);
        this.rgoProduceSystem = new ResourceGatheringOperationProduceSystem(this.gameContext.getEcsWorld(), buildingService);
    }
}

package com.populaire.projetguerrefroide.service;

import com.populaire.projetguerrefroide.system.economy.*;

public class EconomyService {
    private final GameContext gameContext;
    private final ResourceGatheringOperationSizeSystem rgoSizeSystem;
    private final ResourceGatheringOperationHireSystem rgoHireSystem;
    private final ResourceGatheringOperationProduceSystem rgoProduceSystem;

    public EconomyService(GameContext gameContext, ResourceGatheringOperationSizeSystem rgoSizeSystem, ResourceGatheringOperationHireSystem rgoHireSystem, ResourceGatheringOperationProduceSystem rgoProduceSystem) {
        this.gameContext = gameContext;
        this.rgoSizeSystem = rgoSizeSystem;
        this.rgoHireSystem = rgoHireSystem;
        this.rgoProduceSystem = rgoProduceSystem;

    }
}

package com.populaire.projetguerrefroide.service;

import com.populaire.projetguerrefroide.system.economy.*;

/**
 * Orchestre la pipeline économique complète :
 *   PreUpdate : ResetLocalMarketSystem
 *   OnUpdate  : RGOSizeSystem (EcsOnStart) → RGOHireSystem → RGOProduceSystem
 *             → BuildingConsumeSystem → LocalMarketBalanceSystem
 *             → GoodPriceSystem → BuildingRevenueSystem
 */
public class EconomyService {
    private final GameContext gameContext;
    private final ResourceGatheringOperationSizeSystem rgoSizeSystem;
    private final ResourceGatheringOperationHireSystem rgoHireSystem;
    private final ResourceGatheringOperationProduceSystem rgoProduceSystem;
    private final ResetLocalMarketSystem resetLocalMarketSystem;
    private final BuildingConsumeSystem buildingConsumeSystem;
    private final LocalMarketBalanceSystem localMarketBalanceSystem;

    public EconomyService(GameContext gameContext,
                          ResourceGatheringOperationSizeSystem rgoSizeSystem,
                          ResourceGatheringOperationHireSystem rgoHireSystem,
                          ResourceGatheringOperationProduceSystem rgoProduceSystem,
                          ResetLocalMarketSystem resetLocalMarketSystem,
                          BuildingConsumeSystem buildingConsumeSystem,
                          LocalMarketBalanceSystem localMarketBalanceSystem) {
        this.gameContext = gameContext;
        this.rgoSizeSystem = rgoSizeSystem;
        this.rgoHireSystem = rgoHireSystem;
        this.rgoProduceSystem = rgoProduceSystem;
        this.resetLocalMarketSystem = resetLocalMarketSystem;
        this.buildingConsumeSystem = buildingConsumeSystem;
        this.localMarketBalanceSystem = localMarketBalanceSystem;
    }
}

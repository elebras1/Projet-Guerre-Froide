package com.populaire.projetguerrefroide.service;

import com.populaire.projetguerrefroide.economy.production.ResourceGatheringOperationSystem;
import com.populaire.projetguerrefroide.map.World;

public class EconomyService {
    private final WorldService worldService;
    private final ResourceGatheringOperationSystem rgoSystem;

    public EconomyService(WorldService worldService) {
        this.worldService = worldService;
        this.rgoSystem = new ResourceGatheringOperationSystem();
    }

    public void initialize() {
        World world = this.worldService.getWorld();
        this.rgoSystem.initialiaseSize(world.getProvinceStore(), world.getGoodStore(), world.getProductionTypeStore(), world.getEmployeeStore());
    }
}

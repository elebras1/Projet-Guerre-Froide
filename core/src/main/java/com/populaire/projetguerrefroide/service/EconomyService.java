package com.populaire.projetguerrefroide.service;

import com.populaire.projetguerrefroide.economy.production.ResourceGatheringOperationSystem;

public class EconomyService {
    private final ResourceGatheringOperationSystem rgoSystem;

    public EconomyService() {
        this.rgoSystem = new ResourceGatheringOperationSystem();
    }
}

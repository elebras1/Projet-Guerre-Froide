package com.populaire.projetguerrefroide.service;

import com.populaire.projetguerrefroide.economy.production.ResourceGatheringOperationSystem;
import com.populaire.projetguerrefroide.map.WorldContext;

public class EconomyService {
    private final WorldContext worldContext;
    private ResourceGatheringOperationSystem rgoSystem;

    public EconomyService(WorldContext worldContext) {
        this.worldContext = worldContext;
        this.rgoSystem = new ResourceGatheringOperationSystem();
    }

    public void initialize() {
        this.rgoSystem.initialiazeSize(this.worldContext.getProvinceStore(), this.worldContext.getGoodStore(), this.worldContext.getProductionTypeStore(), this.worldContext.getEmployeeStore());
    }

    public void hire() {
        this.rgoSystem.hire(this.worldContext.getProvinceStore(), this.worldContext.getGoodStore(), this.worldContext.getProductionTypeStore(), this.worldContext.getEmployeeStore());
    }

    public void produce() {
        this.rgoSystem.produce(this.worldContext.getProvinceStore(), this.worldContext.getGoodStore(), this.worldContext.getProductionTypeStore());
    }

    public float getResourceGoodsProduction(short provinceId) {
        int provinceIndex = this.worldContext.getProvinceStore().getIndexById().get(provinceId);
        return this.worldContext.getProvinceStore().getResourceGoodsProduction().get(provinceIndex);
    }
}

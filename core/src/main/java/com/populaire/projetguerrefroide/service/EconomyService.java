package com.populaire.projetguerrefroide.service;

import com.populaire.projetguerrefroide.economy.building.EmployeeStore;
import com.populaire.projetguerrefroide.economy.building.ProductionTypeStore;
import com.populaire.projetguerrefroide.economy.good.GoodStore;
import com.populaire.projetguerrefroide.economy.production.ResourceGatheringOperationSystem;
import com.populaire.projetguerrefroide.map.ProvinceStore;

public class EconomyService {
    private final ProvinceStore provinceStore;
    private final GoodStore goodStore;
    private final ProductionTypeStore productionTypeStore;
    private final EmployeeStore employeeStore;
    private final ResourceGatheringOperationSystem rgoSystem;

    public EconomyService(ProvinceStore provinceStore, GoodStore goodStore, ProductionTypeStore productionTypeStore, EmployeeStore employeeStore) {
        this.provinceStore = provinceStore;
        this.goodStore = goodStore;
        this.productionTypeStore = productionTypeStore;
        this.employeeStore = employeeStore;
        this.rgoSystem = new ResourceGatheringOperationSystem();
    }

    public void initialize() {
        this.rgoSystem.initialiaseSize(this.provinceStore, this.goodStore, this.productionTypeStore, this.employeeStore);
    }
}

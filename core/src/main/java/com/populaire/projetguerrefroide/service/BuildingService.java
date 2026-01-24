package com.populaire.projetguerrefroide.service;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.ProductionType;
import com.populaire.projetguerrefroide.component.ProductionTypeView;
import com.populaire.projetguerrefroide.component.ResourceProduction;
import com.populaire.projetguerrefroide.component.ResourceProductionView;

public class BuildingService {
    private final GameContext gameContext;

    public BuildingService(GameContext gameContext) {
        this.gameContext = gameContext;
    }

    public int estimateWorkersForBuilding() {
        return 0;
    }

    public int getMaxWorkers(long resourceGoodId, int resourceGoodSize) {
        World ecsWorld = this.gameContext.getEcsWorld();
        EntityView resourceGoodView = ecsWorld.obtainEntityView(resourceGoodId);
        ResourceProductionView resourceProductionView = resourceGoodView.getMutView(ResourceProduction.class);
        EntityView productionTypeEntityView = ecsWorld.obtainEntityView(resourceProductionView.productionTypeId());
        ProductionTypeView productionTypeDataView = productionTypeEntityView.getMutView(ProductionType.class);
        return resourceGoodSize * productionTypeDataView.workforce();
    }
}

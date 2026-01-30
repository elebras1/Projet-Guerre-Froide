package com.populaire.projetguerrefroide.service;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.dto.BuildingDto;

public class BuildingService {
    private final GameContext gameContext;

    public BuildingService(GameContext gameContext) {
        this.gameContext = gameContext;
    }

    public int estimateWorkersForBuilding() {
        return 0;
    }

    public BuildingDto buildDetails(long buildingId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        Entity building = ecsWorld.obtainEntity(buildingId);
        Building buildingData = building.get(Building.class);
        Entity parent = ecsWorld.obtainEntity(buildingData.parentId());
        Entity buildingType = ecsWorld.obtainEntity(buildingData.typeId());
        EconomyBuilding buildingTypeData = buildingType.get(EconomyBuilding.class);
        return new BuildingDto(buildingId, buildingType.getName(), parent.getName(), buildingTypeData.maxLevel(), buildingTypeData.goodCostIds(), buildingTypeData.goodCostValues(), buildingTypeData.inputGoodIds(), buildingTypeData.inputGoodValues(), buildingTypeData.outputGoodId(), buildingTypeData.outputGoodValue());
    }

    public int getMaxWorkers(long resourceGoodId, int resourceGoodSize) {
        World ecsWorld = this.gameContext.getEcsWorld();
        EntityView resourceGoodView = ecsWorld.obtainEntityView(resourceGoodId);
        ResourceProductionView resourceProductionView = resourceGoodView.getMutView(ResourceProduction.class);
        EntityView productionTypeView = ecsWorld.obtainEntityView(resourceProductionView.productionTypeId());
        ProductionTypeView productionTypeDataView = productionTypeView.getMutView(ProductionType.class);
        return resourceGoodSize * productionTypeDataView.workforce();
    }
}

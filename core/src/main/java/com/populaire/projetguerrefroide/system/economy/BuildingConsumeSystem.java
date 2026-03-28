package com.populaire.projetguerrefroide.system.economy;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.util.FlecsConstants;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.service.GameContext;

public class BuildingConsumeSystem {
    private final World ecsWorld;
    private final GameContext gameContext;

    public BuildingConsumeSystem(World ecsWorld, GameContext gameContext) {
        this.ecsWorld = ecsWorld;
        this.gameContext = gameContext;
        ecsWorld.system("BuildingConsumeSystem")
            .kind(FlecsConstants.EcsOnUpdate)
            .with(Building.class)
            .with(EconomyBuilding.class)
            .with(gameContext.getEcsConstants().suspended()).not()
            .iter(this::consume);
    }

    private void consume(Iter iter) {
        Field<Building> buildingField = iter.field(Building.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            BuildingView buildingView = buildingField.getMutView(i);
            EntityView buildingTypeView = this.ecsWorld.obtainEntityView(buildingView.typeId());
            EconomyBuildingTypeView typeView = buildingTypeView.getMutView(EconomyBuildingType.class);

            EntityView parentView = this.ecsWorld.obtainEntityView(buildingView.parentId());
            LocalMarketView localMarketView = parentView.getMutView(LocalMarket.class);

            int size = buildingView.size();

            for (int index = 0; index < typeView.inputGoodIdsLength(); index++) {
                long goodId = typeView.inputGoodIds(index);
                if (goodId == 0) {
                    break;
                }
                int goodIndex = this.gameContext.getGoodIndex(goodId);
                if (goodIndex < 0) {
                    continue;
                }
                float consumption = typeView.inputGoodValues(index) * size;
                localMarketView.goodConsumptions(goodIndex, localMarketView.goodConsumptions(goodIndex) + consumption);
            }
        }
    }
}


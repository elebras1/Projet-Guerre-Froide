package com.populaire.projetguerrefroide.system.economy;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.util.FlecsConstants;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.service.EconomyRuntime;
import com.populaire.projetguerrefroide.service.GameContext;

public class BuildingConsumeSystem {
    private final EconomyRuntime economyRuntime;

    public BuildingConsumeSystem(World ecsWorld, GameContext gameContext) {
        this.economyRuntime = gameContext.getEconomyRuntime();
        ecsWorld.system("BuildingConsumeSystem")
            .kind(FlecsConstants.EcsOnUpdate)
            .with(Building.class)
            .with(EconomyBuilding.class)
            .with(EconomyHierarchy.class)
            .with(gameContext.getEcsConstants().suspended())
            .not()
            .iter(this::consume);
    }

    private void consume(Iter iter) {
        Field<Building> buildingField = iter.field(Building.class, 0);
        Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 1);
        Field<EconomyHierarchy> economyHierarchyField = iter.field(EconomyHierarchy.class, 2);
        for (int i = 0; i < iter.count(); i++) {
            BuildingView buildingView = buildingField.getMutView(i);
            EconomyBuildingView economyBuildingView = economyBuildingField.getMutView(i);
            EconomyHierarchyView economyHierarchyView = economyHierarchyField.getMutView(i);

            int size = buildingView.size();

            for (int g = 0; g < economyBuildingView.goodInputIndexesLength(); g++) {
                float consumption = economyBuildingView.goodInputValues(g) * size;
                int goodIndex = economyBuildingView.goodInputIndexes(g);
                this.economyRuntime.addMarketGoodConsumptions(economyHierarchyView.localMarketIndex(), goodIndex, consumption);
            }
        }
    }
}


package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.*;
import com.populaire.projetguerrefroide.command.CommandBus;
import com.populaire.projetguerrefroide.command.request.BuildingLevelUpCommand;
import com.populaire.projetguerrefroide.component.*;

public class ExpansionBuildingSystem {
    private final CommandBus commandBus;

    public ExpansionBuildingSystem(World ecsWorld, CommandBus commandBus) {
        this.commandBus = commandBus;
        ecsWorld.system("ExpansionBuildingSystem")
            .kind(Flecs.OnUpdate)
            .with(ExpansionBuilding.class)
            .iter(this::expand);
    }

    private void expand(Iter iter) {
        Field<ExpansionBuilding> expansionBuildingField = iter.field(ExpansionBuilding.class, 0);
        for(int i = 0; i < iter.count(); i++) {
            ExpansionBuildingMutView expansionBuildingDataView = expansionBuildingField.getMutView(i);
            if(expansionBuildingDataView.timeLeft() <= 0) {
                EntityView buildingView = iter.world().obtainEntityView(expansionBuildingDataView.buildingId());
                BuildingMutView buildingViewData = buildingView.getMutView(Building.class);
                int newSize = buildingViewData.size() + 1;
                buildingViewData.size(newSize);
                this.commandBus.dispatch(new BuildingLevelUpCommand(expansionBuildingDataView.buildingId()));

                if(expansionBuildingDataView.levelsQueued() > 1) {
                    expansionBuildingDataView.levelsQueued(expansionBuildingDataView.levelsQueued() - 1);
                    if(buildingView.has(EconomyBuildingType.class)) {
                        EconomyBuildingTypeView economyBuildingTypeViewData = buildingView.getView(EconomyBuildingType.class);
                        expansionBuildingDataView.timeLeft(economyBuildingTypeViewData.time());
                    } else if(buildingView.has(DevelopmentBuildingType.class)) {
                        DevelopmentBuildingTypeView developmentBuildingTypeViewData = buildingView.getView(DevelopmentBuildingType.class);
                        expansionBuildingDataView.timeLeft(developmentBuildingTypeViewData.time());
                    } else if(buildingView.has(SpecialBuildingType.class)) {
                        SpecialBuildingTypeView specialBuildingTypeViewData = buildingView.getView(SpecialBuildingType.class);
                        expansionBuildingDataView.timeLeft(specialBuildingTypeViewData.time());
                    }
                } else {
                    iter.world().obtainEntityView(iter.entity(i)).destruct();
                }
            } else {
                expansionBuildingDataView.timeLeft(expansionBuildingDataView.timeLeft() - 1);
            }
        }
    }
}

package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.util.FlecsConstants;
import com.populaire.projetguerrefroide.command.CommandBus;
import com.populaire.projetguerrefroide.command.request.BuildingLevelUpCommand;
import com.populaire.projetguerrefroide.component.*;

public class ExpandBuildingSystem {
    private final CommandBus commandBus;

    public ExpandBuildingSystem(World ecsWorld, CommandBus commandBus) {
        this.commandBus = commandBus;
        ecsWorld.system("ExpandBuildingSystem")
            .kind(FlecsConstants.EcsOnUpdate)
            .with(ExpansionBuilding.class)
            .iter(this::expand);
    }

    private void expand(Iter iter) {
        Field<ExpansionBuilding> expansionBuildingField = iter.field(ExpansionBuilding.class, 0);
        for(int i = 0; i < iter.count(); i++) {
            ExpansionBuildingView expansionBuildingDataView = expansionBuildingField.getMutView(i);
            if(expansionBuildingDataView.timeLeft() <= 0) {
                EntityView buildingView = iter.world().obtainEntityView(expansionBuildingDataView.buildingId());
                BuildingView buildingViewData = buildingView.getMutView(Building.class);
                EntityView buildingTypeView = iter.world().obtainEntityView(buildingViewData.typeId());
                int newSize = buildingViewData.size() + 1;
                buildingViewData.size(newSize);
                this.commandBus.dispatch(new BuildingLevelUpCommand(expansionBuildingDataView.buildingId()));

                if(expansionBuildingDataView.levelsQueued() > 1) {
                    expansionBuildingDataView.levelsQueued(expansionBuildingDataView.levelsQueued() - 1);
                    if(buildingTypeView.has(EconomyBuildingType.class)) {
                        EconomyBuildingTypeView economyBuildingTypeViewData = buildingTypeView.getMutView(EconomyBuildingType.class);
                        expansionBuildingDataView.timeLeft(economyBuildingTypeViewData.time());
                    } else if(buildingTypeView.has(DevelopmentBuildingType.class)) {
                        DevelopmentBuildingTypeView developmentBuildingTypeViewData = buildingTypeView.getMutView(DevelopmentBuildingType.class);
                        expansionBuildingDataView.timeLeft(developmentBuildingTypeViewData.time());
                    } else if(buildingTypeView.has(SpecialBuildingType.class)) {
                        SpecialBuildingTypeView specialBuildingTypeViewData = buildingTypeView.getMutView(SpecialBuildingType.class);
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

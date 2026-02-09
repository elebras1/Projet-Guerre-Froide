package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.util.FlecsConstants;
import com.populaire.projetguerrefroide.component.*;

public class ExpandBuildingSystem {
    private final World ecsWorld;

    public ExpandBuildingSystem(World ecsWorld) {
        this.ecsWorld = ecsWorld;
        ecsWorld.system("ExpandBuildingSystem").kind(FlecsConstants.EcsOnUpdate).with(ExpansionBuilding.class).multiThreaded().iter(this::expand);
    }

    private void expand(Iter iter) {
        Field<ExpansionBuilding> expansionBuildingField = iter.field(ExpansionBuilding.class, 0);
        for(int i = 0; i < iter.count(); i++) {
            ExpansionBuildingView expansionBuildingDataView = expansionBuildingField.getMutView(i);
            if(expansionBuildingDataView.timeLeft() <= 0) {
                EntityView buildingView = this.ecsWorld.obtainEntityView(expansionBuildingDataView.buildingId());
                BuildingView buildingViewData = buildingView.getMutView(Building.class);
                EntityView buildingTypeView = this.ecsWorld.obtainEntityView(buildingViewData.typeId());
                int newSize = buildingViewData.size() + 1;
                buildingViewData.size(newSize);

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
                    this.ecsWorld.obtainEntityView(iter.entity(i)).destruct();
                }
            } else {
                expansionBuildingDataView.timeLeft(expansionBuildingDataView.timeLeft() - 1);
            }
        }
    }
}

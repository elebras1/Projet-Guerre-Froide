package com.populaire.projetguerrefroide.simulation.command;

import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.protocol.command.*;
import com.populaire.projetguerrefroide.system.EconomyPipelines;
import com.populaire.projetguerrefroide.util.ConstructionUtils;
import com.populaire.projetguerrefroide.simulation.EcsConstants;
import com.populaire.projetguerrefroide.util.EconomyBuildingUtils;
import com.populaire.projetguerrefroide.simulation.SimulationContext;
import com.populaire.projetguerrefroide.simulation.SimulationState;
import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Flecs;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;

public final class CommandService {
    private final World ecsWorld;
    private final EcsConstants ecsConstants;
    private final EconomyPipelines economyPipelines;
    private final SimulationState state;
    private final Query constructionSiteQuery;
    private int levelsQueued;

    public CommandService(SimulationContext simulationContext, EconomyPipelines economyPipelines, SimulationState state) {
        this.ecsWorld = simulationContext.getEcsWorld();
        this.ecsConstants = simulationContext.getEcsConstants();
        this.economyPipelines = economyPipelines;
        this.state = state;
        this.constructionSiteQuery = this.ecsWorld.query().with(ConstructionSite.class).build();
    }

    public boolean handle(GameCommand command) {
        return switch (command) {
            case ChoosePlayerCountryCommand choose -> this.choosePlayerCountry(choose.countryId());
            case ExpandBuildingCommand expand -> this.expandBuilding(expand.buildingId());
            case SuspendBuildingCommand suspend -> this.suspendBuilding(suspend.buildingId());
            case ResumeBuildingCommand resume -> this.resumeBuilding(resume.buildingId());
            case DemolishBuildingCommand demolish -> this.demolishBuilding(demolish.buildingId());
        };
    }

    private boolean choosePlayerCountry(long countryId) {
        if (countryId == 0 || !this.ecsWorld.isAlive(countryId) || !this.ecsWorld.obtainEntityView(countryId).has(Country.class)) {
            return false;
        }
        this.state.playerCountryId(countryId);
        if (!this.state.economyInitialized()) {
            this.economyPipelines.initialize();
            this.state.markEconomyInitialized();
        }
        return true;
    }

    private boolean isPlayerBuilding(long buildingId) {
        if (buildingId == 0 || !this.ecsWorld.isAlive(buildingId)) {
            return false;
        }
        EntityView building = this.ecsWorld.obtainEntityView(buildingId);
        if (!building.has(Building.class)) {
            return false;
        }
        return building.<BuildingMutView>getMutView(Building.class).countryId() == this.state.playerCountryId();
    }

    private boolean stateControlled(EntityView building) {
        if (!building.has(EconomyBuilding.class)) {
            return true;
        }
        EconomyBuildingView economyBuilding = building.getMutView(EconomyBuilding.class);
        return economyBuilding.ownerTagId() == this.ecsConstants.countryTag();
    }

    private int countLevelsQueued(long buildingId) {
        this.levelsQueued = 0;
        this.constructionSiteQuery.iter(iter -> {
            Field<ConstructionSite> siteField = iter.field(ConstructionSite.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                ConstructionSiteView site = siteField.getMutView(i);
                if (site.buildingId() == buildingId) {
                    this.levelsQueued++;
                }
            }
        });
        return this.levelsQueued;
    }

    private boolean demolishBuilding(long buildingId) {
        if (!this.isPlayerBuilding(buildingId) || !this.stateControlled(this.ecsWorld.obtainEntityView(buildingId))) {
            return false;
        }
        EconomyBuildingUtils.releaseAssets(this.ecsWorld, this.ecsConstants, buildingId);
        Entity building = this.ecsWorld.obtainEntity(buildingId);
        building.destruct();
        return true;
    }

    private boolean expandBuilding(long buildingId) {
        if (!this.isPlayerBuilding(buildingId)) {
            return false;
        }
        EntityView building = this.ecsWorld.obtainEntityView(buildingId);
        if (!this.stateControlled(building)) {
            return false;
        }
        BuildingView buildingData = building.getMutView(Building.class);
        long buildingTypeId = building.target(Flecs.IsA, 0);
        int size = buildingData.size();
        long parentId = buildingData.parentId();
        long countryId = buildingData.countryId();
        int maxLevel;
        if (building.has(EconomyBuildingType.class)) {
            maxLevel = building.<EconomyBuildingTypeView>getView(EconomyBuildingType.class).maxLevel();
        } else if (building.has(SpecialBuildingType.class)) {
            maxLevel = building.<SpecialBuildingTypeView>getView(SpecialBuildingType.class).maxLevel();
        } else {
            maxLevel = building.<DevelopmentBuildingTypeView>getView(DevelopmentBuildingType.class).maxLevel();
        }
        if (size + this.countLevelsQueued(buildingId) >= maxLevel) {
            return false;
        }
        ConstructionUtils.startSite(this.ecsWorld, this.ecsConstants, buildingId, buildingTypeId, parentId, countryId, this.ecsConstants.countryTag());
        return true;
    }

    private boolean suspendBuilding(long buildingId) {
        if (!this.isPlayerBuilding(buildingId) || !this.stateControlled(this.ecsWorld.obtainEntityView(buildingId))) {
            return false;
        }
        this.ecsWorld.obtainEntity(buildingId).add(this.ecsConstants.suspended());
        return true;
    }

    private boolean resumeBuilding(long buildingId) {
        if (!this.isPlayerBuilding(buildingId) || !this.stateControlled(this.ecsWorld.obtainEntityView(buildingId))) {
            return false;
        }
        this.ecsWorld.obtainEntity(buildingId).remove(this.ecsConstants.suspended());
        return true;
    }
}

package com.populaire.projetguerrefroide.service;

import io.github.elebras1.flecs.Flecs;
import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.dto.BuildingDto;
import com.populaire.projetguerrefroide.dto.BuildingSummaryDto;
import com.populaire.projetguerrefroide.util.ConstructionUtils;

public class BuildingService {
    private final GameContext gameContext;
    private final Query constructionSiteQuery;

    public BuildingService(GameContext gameContext) {
        this.gameContext = gameContext;
        this.constructionSiteQuery = gameContext.getEcsWorld().query().with(ConstructionSite.class).build();
    }

    private int countLevelsQueued(long buildingId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        int[] levelsQueued = new int[1];
        this.constructionSiteQuery.each((long siteId) -> {
            ConstructionSiteView site = ecsWorld.obtainEntityView(siteId).getView(ConstructionSite.class);
            if (site.buildingId() == buildingId) {
                levelsQueued[0]++;
            }
        });
        return levelsQueued[0];
    }

    public int estimateWorkersForBuilding() {
        return 0;
    }

    private boolean stateControlled(EntityView building) {
        if (!building.has(EconomyBuilding.class)) {
            return true;
        }
        EconomyBuildingView economyBuilding = building.getMutView(EconomyBuilding.class);
        return economyBuilding.ownerTagId() == this.gameContext.getEcsConstants().countryTag();
    }

    public BuildingSummaryDto buildSummary(long buildingId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        EntityView building = ecsWorld.obtainEntityView(buildingId);
        BuildingView buildingData = building.getMutView(Building.class);
        EntityView buildingType = ecsWorld.obtainEntityView(building.target(Flecs.IsA, 0));
        EconomyBuildingTypeView buildingTypeData = buildingType.getMutView(EconomyBuildingType.class);
        int levelsQueued = this.countLevelsQueued(buildingId);
        boolean isSuspended = building.has(this.gameContext.getEcsConstants().suspended());
        EconomyBuildingView economyBuilding = building.getMutView(EconomyBuilding.class);
        return new BuildingSummaryDto(buildingId, buildingType.name(), buildingData.size(), buildingTypeData.maxLevel(), economyBuilding.primaryWorkerAmount() + economyBuilding.secondaryWorkerAmount(), economyBuilding.production(), levelsQueued, isSuspended);
    }

    public BuildingDto buildDetails(long buildingId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        Entity building = ecsWorld.obtainEntity(buildingId);
        EconomyBuilding economyBuilding = building.get(EconomyBuilding.class);
        Building buildingData = building.get(Building.class);
        Entity parent = ecsWorld.obtainEntity(buildingData.parentId());
        Entity buildingType = ecsWorld.obtainEntity(building.target(Flecs.IsA, 0));
        EconomyBuildingType buildingTypeData = buildingType.get(EconomyBuildingType.class);
        String[] goodCostNameIds = new String[buildingTypeData.goodCostIds().length];
        for(int i = 0; i < buildingTypeData.goodCostIds().length; i++) {
            long goodId = buildingTypeData.goodCostIds()[i];
            if(goodId != 0) {
                Entity goodEntity = ecsWorld.obtainEntity(goodId);
                goodCostNameIds[i] = goodEntity.name();
            }
        }
        String[] inputGoodNameIds = new String[buildingTypeData.goodInputIds().length];
        for(int i = 0; i < buildingTypeData.goodInputIds().length; i++) {
            long goodId = buildingTypeData.goodInputIds()[i];
            if(goodId != 0) {
                Entity goodEntity = ecsWorld.obtainEntity(goodId);
                inputGoodNameIds[i] = goodEntity.name();
            }
        }
        Entity outputGoodEntity = ecsWorld.obtainEntity(buildingTypeData.goodOutputId());
        String outputGoodNameId = outputGoodEntity.name();
        int amountWorkers = economyBuilding.primaryWorkerAmount() + economyBuilding.secondaryWorkerAmount();
        int maxWorkers = buildingData.size() * buildingTypeData.workforce();
        return new BuildingDto(buildingId, buildingType.name(), parent.name(), buildingTypeData.maxLevel(), goodCostNameIds, buildingTypeData.goodCostAmounts(), inputGoodNameIds, buildingTypeData.goodInputAmounts(), outputGoodNameId, buildingTypeData.goodOutputAmount(), amountWorkers, maxWorkers, building.has(this.gameContext.getEcsConstants().suspended()),
            this.stateControlled(ecsWorld.obtainEntityView(buildingId)));
    }

    public void demolishBuilding(long buildingId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        if (!this.stateControlled(ecsWorld.obtainEntityView(buildingId))) {
            return;
        }
        Entity building = ecsWorld.obtainEntity(buildingId);
        building.destruct();
    }

    public void expandBuilding(long buildingId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        EntityView building = ecsWorld.obtainEntityView(buildingId);
        if (!this.stateControlled(building)) {
            return;
        }
        BuildingView buildingData = building.getMutView(Building.class);
        long buildingTypeId = building.target(Flecs.IsA, 0);
        int maxLevel;
        if (building.has(EconomyBuildingType.class)) {
            EconomyBuildingTypeView typeData = building.getView(EconomyBuildingType.class);
            maxLevel = typeData.maxLevel();
        } else {
            DevelopmentBuildingTypeView typeData = building.getView(DevelopmentBuildingType.class);
            maxLevel = typeData.maxLevel();
        }
        if (buildingData.size() + this.countLevelsQueued(buildingId) < maxLevel) {
            ConstructionUtils.startSite(ecsWorld, this.gameContext.getEcsConstants(), buildingId, buildingTypeId, buildingData.parentId(), buildingData.countryId(), this.gameContext.getEcsConstants().countryTag());
        }
    }

    public void suspendBuilding(long buildingId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        if (!this.stateControlled(ecsWorld.obtainEntityView(buildingId))) {
            return;
        }
        Entity building = ecsWorld.obtainEntity(buildingId);
        building.add(this.gameContext.getEcsConstants().suspended());
    }

    public void resumeBuilding(long buildingId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        if (!this.stateControlled(ecsWorld.obtainEntityView(buildingId))) {
            return;
        }
        Entity building = ecsWorld.obtainEntity(buildingId);
        building.remove(this.gameContext.getEcsConstants().suspended());
    }
}

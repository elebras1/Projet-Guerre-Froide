package com.populaire.projetguerrefroide.service;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.dto.BuildingDto;
import com.populaire.projetguerrefroide.dto.BuildingSummaryDto;
import com.populaire.projetguerrefroide.system.ExpansionBuildingSystem;

public class BuildingService {
    private final GameContext gameContext;
    private final ExpansionBuildingSystem expandBuildingSystem;

    public BuildingService(GameContext gameContext, ExpansionBuildingSystem expandBuildingSystem) {
        this.gameContext = gameContext;
        this.expandBuildingSystem = expandBuildingSystem;
    }

    public int estimateWorkersForBuilding() {
        return 0;
    }

    public BuildingSummaryDto buildSummary(long buildingId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        EntityView building = ecsWorld.obtainEntityView(buildingId);
        BuildingView buildingData = building.getMutView(Building.class);
        EntityView buildingType = ecsWorld.obtainEntityView(buildingData.typeId());
        EconomyBuildingTypeView buildingTypeData = buildingType.getMutView(EconomyBuildingType.class);
        int levelsQueued = 0;
        long expansionBuildingId = ecsWorld.lookup("expand_" + buildingId);
        if (expansionBuildingId != 0) {
            EntityView expansionBuilding = ecsWorld.obtainEntityView(expansionBuildingId);
            ExpansionBuildingView expansionData = expansionBuilding.getMutView(ExpansionBuilding.class);
            levelsQueued = expansionData.levelsQueued();
        }
        boolean isSuspended = building.has(this.gameContext.getEcsConstants().suspended());
        EconomyBuildingView economyBuilding = building.getMutView(EconomyBuilding.class);
        return new BuildingSummaryDto(buildingId, buildingType.getName(), buildingData.size(), buildingTypeData.maxLevel(), economyBuilding.primaryWorkerAmount() + economyBuilding.secondaryWorkerAmount(), economyBuilding.production(), levelsQueued, isSuspended);
    }

    public BuildingDto buildDetails(long buildingId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        Entity building = ecsWorld.obtainEntity(buildingId);
        EconomyBuilding economyBuilding = building.get(EconomyBuilding.class);
        Building buildingData = building.get(Building.class);
        Entity parent = ecsWorld.obtainEntity(buildingData.parentId());
        Entity buildingType = ecsWorld.obtainEntity(buildingData.typeId());
        EconomyBuildingType buildingTypeData = buildingType.get(EconomyBuildingType.class);
        String[] goodCostNameIds = new String[buildingTypeData.goodCostIds().length];
        for(int i = 0; i < buildingTypeData.goodCostIds().length; i++) {
            long goodId = buildingTypeData.goodCostIds()[i];
            if(goodId != 0) {
                Entity goodEntity = ecsWorld.obtainEntity(goodId);
                goodCostNameIds[i] = goodEntity.getName();
            }
        }
        String[] inputGoodNameIds = new String[buildingTypeData.goodInputIds().length];
        for(int i = 0; i < buildingTypeData.goodInputIds().length; i++) {
            long goodId = buildingTypeData.goodInputIds()[i];
            if(goodId != 0) {
                Entity goodEntity = ecsWorld.obtainEntity(goodId);
                inputGoodNameIds[i] = goodEntity.getName();
            }
        }
        Entity outputGoodEntity = ecsWorld.obtainEntity(buildingTypeData.goodOutputId());
        String outputGoodNameId = outputGoodEntity.getName();
        int amountWorkers = economyBuilding.primaryWorkerAmount() + economyBuilding.secondaryWorkerAmount();
        int maxWorkers = buildingData.size() * buildingTypeData.workforce();
        return new BuildingDto(buildingId, buildingType.getName(), parent.getName(), buildingTypeData.maxLevel(), goodCostNameIds, buildingTypeData.goodCostAmounts(), inputGoodNameIds, buildingTypeData.goodInputAmounts(), outputGoodNameId, buildingTypeData.goodOutputAmount(), amountWorkers, maxWorkers, building.has(this.gameContext.getEcsConstants().suspended()));
    }

    public void demolishBuilding(long buildingId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        Entity building = ecsWorld.obtainEntity(buildingId);
        building.destruct();
    }

    public void expandBuilding(long buildingId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        Entity building = ecsWorld.obtainEntity(buildingId);
        Building buildingData = building.get(Building.class);
        Entity country = ecsWorld.obtainEntity(buildingData.countryId());
        CountryEffectPolicy countryEffectPolicy = country.get(CountryEffectPolicy.class);
        Entity buildingType = ecsWorld.obtainEntity(buildingData.typeId());

        int maxLevel = 0;
        int baseTime = 0;

        if (buildingType.has(EconomyBuildingType.class)) {
            EconomyBuildingType typeData = buildingType.get(EconomyBuildingType.class);
            baseTime = (int) (typeData.time() * (1f - countryEffectPolicy.constructionSpeed()));
            maxLevel = typeData.maxLevel();
        } else if (buildingType.has(DevelopmentBuildingType.class)) {
            DevelopmentBuildingType typeData = buildingType.get(DevelopmentBuildingType.class);
            baseTime = (int) (typeData.time() * (1f - countryEffectPolicy.constructionSpeed()));
            maxLevel = typeData.maxLevel();
        }

        long expansionBuildingId = ecsWorld.lookup("expand_" + buildingId);
        Entity expansionBuilding;
        int levelsQueued = 0;
        int currentTimeLeft;

        if (expansionBuildingId != 0) {
            expansionBuilding = ecsWorld.obtainEntity(expansionBuildingId);
            ExpansionBuilding expansionData = expansionBuilding.get(ExpansionBuilding.class);
            levelsQueued = expansionData.levelsQueued();
            currentTimeLeft = expansionData.timeLeft();
        } else {
            expansionBuildingId = ecsWorld.entity("expand_" + buildingId);
            expansionBuilding = ecsWorld.obtainEntity(expansionBuildingId);
            currentTimeLeft = baseTime;
        }

        if (buildingData.size() + levelsQueued < maxLevel) {
            levelsQueued++;
            expansionBuilding.set(new ExpansionBuilding(buildingId, currentTimeLeft, levelsQueued));
        }
    }

    public void suspendBuilding(long buildingId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        Entity building = ecsWorld.obtainEntity(buildingId);
        building.add(this.gameContext.getEcsConstants().suspended());
    }

    public void resumeBuilding(long buildingId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        Entity building = ecsWorld.obtainEntity(buildingId);
        building.remove(this.gameContext.getEcsConstants().suspended());
    }
}

package com.populaire.projetguerrefroide.service;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.dto.BuildingDto;
import com.populaire.projetguerrefroide.system.ExpandBuildingSystem;

public class BuildingService {
    private final GameContext gameContext;
    private final ExpandBuildingSystem expandBuildingSystem;

    public BuildingService(GameContext gameContext, ExpandBuildingSystem expandBuildingSystem) {
        this.gameContext = gameContext;
        this.expandBuildingSystem = expandBuildingSystem;
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
        EconomyBuildingType buildingTypeData = buildingType.get(EconomyBuildingType.class);
        ProductionType productionType = ecsWorld.obtainEntity(buildingTypeData.productionTypeId()).get(ProductionType.class);
        String[] goodCostNameIds = new String[buildingTypeData.goodCostIds().length];
        for(int i = 0; i < buildingTypeData.goodCostIds().length; i++) {
            long goodId = buildingTypeData.goodCostIds()[i];
            if(goodId != 0) {
                Entity goodEntity = ecsWorld.obtainEntity(goodId);
                goodCostNameIds[i] = goodEntity.getName();
            }
        }
        String[] inputGoodNameIds = new String[buildingTypeData.inputGoodIds().length];
        for(int i = 0; i < buildingTypeData.inputGoodIds().length; i++) {
            long goodId = buildingTypeData.inputGoodIds()[i];
            if(goodId != 0) {
                Entity goodEntity = ecsWorld.obtainEntity(goodId);
                inputGoodNameIds[i] = goodEntity.getName();
            }
        }
        Entity outputGoodEntity = ecsWorld.obtainEntity(buildingTypeData.outputGoodId());
        String outputGoodNameId = outputGoodEntity.getName();
        int amountWorkers = this.getAmountWorkers(productionType);
        int maxWorkers = this.getMaxWorkers(buildingTypeData, buildingData.size());
        return new BuildingDto(buildingId, buildingType.getName(), parent.getName(), buildingTypeData.maxLevel(), goodCostNameIds, buildingTypeData.goodCostValues(), inputGoodNameIds, buildingTypeData.inputGoodValues(), outputGoodNameId, buildingTypeData.outputGoodValue(), amountWorkers, maxWorkers);
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
        Entity buildingType = ecsWorld.obtainEntity(buildingData.typeId());

        long expansionBuildingId = ecsWorld.lookup("expand_" + buildingId);
        Entity expansionBuilding = null;
        int timeLeft = 0;
        int levelsQueued = 0;

        if(expansionBuildingId != 0) {
            expansionBuilding = ecsWorld.obtainEntity(expansionBuildingId);
            ExpansionBuilding expansionBuildingData = expansionBuilding.get(ExpansionBuilding.class);
            levelsQueued = expansionBuildingData.levelsQueued();
            timeLeft = expansionBuildingData.timeLeft();
        } else {
            expansionBuildingId = ecsWorld.entity("expand_" + buildingId);
            expansionBuilding = ecsWorld.obtainEntity(expansionBuildingId);
            if(buildingType.has(EconomyBuildingType.class)) {
                EconomyBuildingType economyBuildingTypeData = buildingType.get(EconomyBuildingType.class);
                timeLeft = economyBuildingTypeData.time();
            } else if(buildingType.has(DevelopmentBuildingType.class)) {
                DevelopmentBuildingType developmentBuildinggTypeData = buildingType.get(DevelopmentBuildingType.class);
                timeLeft = developmentBuildinggTypeData.time();
            } else if(buildingType.has(SpecialBuildingType.class)) {
                SpecialBuildingType specialBuildingTypeData = buildingType.get(SpecialBuildingType.class);
                timeLeft = specialBuildingTypeData.time();
            }
        }

        levelsQueued++;

        expansionBuilding.set(new ExpansionBuilding(buildingId, timeLeft, levelsQueued));
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

    public int getMaxWorkers(long buildingId, int size) {
        World ecsWorld = this.gameContext.getEcsWorld();
        EntityView buildingView = ecsWorld.obtainEntityView(buildingId);
        ResourceProductionView resourceProductionView = buildingView.getMutView(ResourceProduction.class);
        EntityView productionTypeView = ecsWorld.obtainEntityView(resourceProductionView.productionTypeId());
        ProductionTypeView productionTypeDataView = productionTypeView.getMutView(ProductionType.class);
        return size * productionTypeDataView.workforce();
    }

    private int getMaxWorkers(EconomyBuildingType buildingTypeData, int size) {
        World ecsWorld = this.gameContext.getEcsWorld();
        Entity productionType = ecsWorld.obtainEntity(buildingTypeData.productionTypeId());
        ProductionType productionTypeDataView = productionType.get(ProductionType.class);
        return size * productionTypeDataView.workforce();
    }

    private  int getAmountWorkers(ProductionType productionTypeData) {
        int totalWorkers = 0;
        for (long employeeTypeId : productionTypeData.employeeTypes()) {
            if (employeeTypeId != 0) {
                Entity employeeType = this.gameContext.getEcsWorld().obtainEntity(employeeTypeId);
                EmployeeType employeeTypeData = employeeType.get(EmployeeType.class);
                totalWorkers += (int) employeeTypeData.amount();
            }
        }
        return totalWorkers;
    }
}

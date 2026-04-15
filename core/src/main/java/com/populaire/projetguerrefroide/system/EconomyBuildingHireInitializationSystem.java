package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.github.tommyettinger.ds.IntIntMap;
import com.populaire.projetguerrefroide.component.*;

public class EconomyBuildingHireInitializationSystem {
    private final World ecsWorld;

    public EconomyBuildingHireInitializationSystem(World ecsWorld, long phaseId) {
        this.ecsWorld = ecsWorld;
        ecsWorld.system("EconomyBuildingHireInitializationSystem")
            .kind(phaseId)
            .with(EconomyBuilding.class)
            .with(Building.class)
            .orderBy(Building.class, (BuildingView buildingA, BuildingView buildingB) -> {
                int compareLocalMarket = Long.compare(buildingA.parentId(), buildingB.parentId());
                if(compareLocalMarket != 0) {
                    return compareLocalMarket;
                }
                return Long.compare(buildingA.typeId(), buildingB.typeId());
            })
            .iter(this::hire);
    }

    private void hire(Iter iter) {
        long buildingTypeId = 0;
        EconomyBuildingTypeView economyBuildingType = null;

        long localMarketId = 0;
        LocalMarketView localMarketData = null;
        DemographicsView demographics = null;

        int primaryWorkerPopTypeIndex = -1;
        int secondaryWorkerPopTypeIndex = -1;

        Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 0);
        Field<Building> buildingField = iter.field(Building.class, 1);

        IntIntMap hiredThisMarket = new IntIntMap();

        for(int i = 0; i < iter.count(); i++) {
            EconomyBuildingView economyBuilding = economyBuildingField.getMutView(i);
            BuildingView building = buildingField.getMutView(i);

            if(building.typeId() != buildingTypeId) {
                buildingTypeId = building.typeId();
                economyBuildingType = this.ecsWorld.obtainEntityView(building.typeId()).getMutView(EconomyBuildingType.class);
                primaryWorkerPopTypeIndex = economyBuildingType.primaryWorkerPopTypeIndex();
                secondaryWorkerPopTypeIndex = economyBuildingType.secondaryWorkerPopTypeIndex();
            }

            if(building.parentId() != localMarketId) {
                localMarketId = building.parentId();
                EntityView localMarket = this.ecsWorld.obtainEntityView(building.parentId());
                localMarketData = localMarket.getMutView(LocalMarket.class);
                demographics = localMarket.getMutView(Demographics.class);
                hiredThisMarket.clear();
            }

            int currentPrimaryEmployed = demographics.employmentByPopType(primaryWorkerPopTypeIndex) + hiredThisMarket.getOrDefault(primaryWorkerPopTypeIndex, 0);
            int currentSecondaryEmployed = demographics.employmentByPopType(secondaryWorkerPopTypeIndex) + hiredThisMarket.getOrDefault(secondaryWorkerPopTypeIndex, 0);

            int primaryPopTypeWorkerTarget = (int) (economyBuildingType.workforce() * building.size() * economyBuildingType.primaryWorkerPopTypeRatio());
            int secondaryPopTypeWorkerTarget = (int) (economyBuildingType.workforce() * building.size() * economyBuildingType.secondaryWorkerPopTypeRatio());

            int primaryAvailable = Math.max(0, demographics.totalByPopType(primaryWorkerPopTypeIndex) - currentPrimaryEmployed);
            int secondaryAvailable = Math.max(0, demographics.totalByPopType(secondaryWorkerPopTypeIndex) - currentSecondaryEmployed);

            int primaryWorkerAmount = Math.min(primaryPopTypeWorkerTarget, primaryAvailable);
            int secondaryWorkerAmount = Math.min(secondaryPopTypeWorkerTarget, secondaryAvailable);

            economyBuilding.primaryWorkerAmount(primaryWorkerAmount);
            economyBuilding.secondaryWorkerAmount(secondaryWorkerAmount);

            hiredThisMarket.put(primaryWorkerPopTypeIndex, hiredThisMarket.getOrDefault(primaryWorkerPopTypeIndex, 0) + primaryWorkerAmount);
            hiredThisMarket.put(secondaryWorkerPopTypeIndex, hiredThisMarket.getOrDefault(secondaryWorkerPopTypeIndex, 0) + secondaryWorkerAmount);

            float primaryWorkerPopTypeEmploymentRatio = (float) (currentPrimaryEmployed + primaryWorkerAmount) / Math.max(1f, demographics.totalByPopType(primaryWorkerPopTypeIndex));
            float secondaryWorkerPopTypeEmploymentRatio = (float) (currentSecondaryEmployed + secondaryWorkerAmount) / Math.max(1f, demographics.totalByPopType(secondaryWorkerPopTypeIndex));

            localMarketData.workerPopTypeEmploymentRatios(primaryWorkerPopTypeIndex, primaryWorkerPopTypeEmploymentRatio);
            localMarketData.workerPopTypeEmploymentRatios(secondaryWorkerPopTypeIndex, secondaryWorkerPopTypeEmploymentRatio);
        }
    }
}

package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
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
                int cmp = Long.compare(buildingA.parentId(), buildingB.parentId());
                if(cmp != 0) {
                    return cmp;
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
        float primaryWorkerPopTypeEmploymentRatio = 0f;
        float secondaryWorkerPopTypeEmploymentRatio = 0f;

        Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 0);
        Field<Building> buildingField = iter.field(Building.class, 1);
        for(int i = 0; i < iter.count(); i++) {
            EconomyBuildingView economyBuilding = economyBuildingField.getMutView(i);
            BuildingView building = buildingField.getMutView(i);

            if(building.typeId() != buildingTypeId) {
                buildingTypeId = building.typeId();
                economyBuildingType = this.ecsWorld.obtainEntityView(building.typeId()).getMutView(EconomyBuildingType.class);

                if(demographics != null) {
                    primaryWorkerPopTypeIndex = economyBuildingType.primaryWorkerPopTypeIndex();
                    secondaryWorkerPopTypeIndex = economyBuildingType.secondaryWorkerPopTypeIndex();
                    primaryWorkerPopTypeEmploymentRatio = (float) demographics.employmentByPopType(primaryWorkerPopTypeIndex) / Math.max(1f, demographics.totalByPopType(primaryWorkerPopTypeIndex));
                    secondaryWorkerPopTypeEmploymentRatio = (float) demographics.employmentByPopType(secondaryWorkerPopTypeIndex) /  Math.max(1f, demographics.totalByPopType(secondaryWorkerPopTypeIndex));
                }
            }

            if(building.parentId() != localMarketId) {
                localMarketId = building.parentId();
                EntityView localMarket = this.ecsWorld.obtainEntityView(building.parentId());
                localMarketData = localMarket.getMutView(LocalMarket.class);
                demographics = localMarket.getMutView(Demographics.class);

                primaryWorkerPopTypeIndex = economyBuildingType.primaryWorkerPopTypeIndex();
                secondaryWorkerPopTypeIndex = economyBuildingType.secondaryWorkerPopTypeIndex();
                primaryWorkerPopTypeEmploymentRatio = (float) demographics.employmentByPopType(primaryWorkerPopTypeIndex) /  Math.max(1f, demographics.totalByPopType(primaryWorkerPopTypeIndex));
                secondaryWorkerPopTypeEmploymentRatio = (float) demographics.employmentByPopType(secondaryWorkerPopTypeIndex) /  Math.max(1f, demographics.totalByPopType(secondaryWorkerPopTypeIndex));
            }

            int primaryPopTypeWorkerTarget = (int) (economyBuildingType.workforce() * economyBuildingType.primaryWorkerPopTypeRatio());
            int secondarPopTypeyWorkerTarget = (int) (economyBuildingType.workforce() * economyBuildingType.secondaryWorkerPopTypeRatio());

            int primaryWorkerAmount = Math.min(primaryPopTypeWorkerTarget, (int) (demographics.totalByPopType(primaryWorkerPopTypeIndex) * (1 - primaryWorkerPopTypeEmploymentRatio)));
            int secondaryWorkerAmount = Math.min(secondarPopTypeyWorkerTarget, (int) (demographics.totalByPopType(secondaryWorkerPopTypeIndex) * (1 - secondaryWorkerPopTypeEmploymentRatio)));

            economyBuilding.primaryWorkerAmount(primaryWorkerAmount);
            economyBuilding.secondaryWorkerAmount(secondaryWorkerAmount);

            primaryWorkerPopTypeEmploymentRatio = (float) (demographics.employmentByPopType(primaryWorkerPopTypeIndex) + primaryWorkerAmount) /  Math.max(1f, demographics.totalByPopType(primaryWorkerPopTypeIndex));
            secondaryWorkerPopTypeEmploymentRatio = (float) (demographics.employmentByPopType(secondaryWorkerPopTypeIndex) + secondaryWorkerAmount) /  Math.max(1f, demographics.totalByPopType(secondaryWorkerPopTypeIndex));
            localMarketData.workerPopTypeEmploymentRatios(primaryWorkerPopTypeIndex, primaryWorkerPopTypeEmploymentRatio);
            localMarketData.workerPopTypeEmploymentRatios(secondaryWorkerPopTypeIndex, secondaryWorkerPopTypeEmploymentRatio);

            System.out.println("EconomyBuildingHireInitializationSystem: Hired " + primaryWorkerAmount + " primary workers and " + secondaryWorkerAmount + " secondary workers for building " + iter.entityId(i) + ", Employment ratios updated: " + primaryWorkerPopTypeEmploymentRatio + " for primary pop type and " + secondaryWorkerPopTypeEmploymentRatio + " for secondary pop type");
        }
    }
}

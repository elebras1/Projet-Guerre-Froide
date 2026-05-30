package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.github.tommyettinger.ds.LongObjectMap;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.Constants.POP_TYPE_COUNT;

public class EconomyBuildingEmploymentSystem {

    public EconomyBuildingEmploymentSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("EconomyBuildingEmploymentSystem")
            .kind(phaseId)
            .with(EconomyBuilding.class)
            .with(Building.class)
            .orderBy(EconomyBuilding.class, (EconomyBuildingView buildingA, EconomyBuildingView buildingB) -> -Float.compare(buildingA.profit(), buildingB.profit()))
            .iter(this::hire);
    }

    private void hire(Iter iter) {
        long buildingTypeId = 0;
        EconomyBuildingTypeView economyBuildingType = null;

        long regionInstanceId = 0;
        RegionInstanceView regionInstanceData = null;
        RegionInstanceIncomeView regionInstanceIncome = null;
        DemographicsView demographics = null;

        int primaryWorkerPopTypeIndex = -1;
        int secondaryWorkerPopTypeIndex = -1;

        Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 0);
        Field<Building> buildingField = iter.field(Building.class, 1);

        for(int i = 0; i < iter.count(); i++) {
            EconomyBuildingView economyBuilding = economyBuildingField.getMutView(i);
            BuildingView building = buildingField.getMutView(i);

            if(building.typeId() != buildingTypeId) {
                buildingTypeId = building.typeId();
                economyBuildingType = iter.world().obtainEntityView(building.typeId()).getMutView(EconomyBuildingType.class);
                primaryWorkerPopTypeIndex = economyBuildingType.primaryWorkerPopTypeIndex();
                secondaryWorkerPopTypeIndex = economyBuildingType.secondaryWorkerPopTypeIndex();
            }

            if(building.parentId() != regionInstanceId) {
                regionInstanceId = building.parentId();
                EntityView regionInstance = iter.world().obtainEntityView(building.parentId());
                regionInstanceData = regionInstance.getMutView(RegionInstance.class);
                regionInstanceIncome = regionInstance.getMutView(RegionInstanceIncome.class);
                demographics = regionInstance.getMutView(Demographics.class);
            }

            int currentPrimaryEmployed = regionInstanceIncome.workersByPopType(primaryWorkerPopTypeIndex);
            int currentSecondaryEmployed = regionInstanceIncome.workersByPopType(secondaryWorkerPopTypeIndex);

            int primaryPopTypeWorkerTarget = (int) (economyBuildingType.workforce() * building.size() * economyBuildingType.primaryWorkerPopTypeRatio() * economyBuilding.scale());
            int secondaryPopTypeWorkerTarget = (int) (economyBuildingType.workforce() * building.size() * economyBuildingType.secondaryWorkerPopTypeRatio() * economyBuilding.scale());

            int primaryAvailable = Math.max(0, demographics.totalByPopType(primaryWorkerPopTypeIndex) - currentPrimaryEmployed);
            int secondaryAvailable = Math.max(0, demographics.totalByPopType(secondaryWorkerPopTypeIndex) - currentSecondaryEmployed);

            int primaryWorkerAmount = Math.min(primaryPopTypeWorkerTarget, primaryAvailable);
            int secondaryWorkerAmount = Math.min(secondaryPopTypeWorkerTarget, secondaryAvailable);

            economyBuilding.primaryWorkerAmount(primaryWorkerAmount);
            economyBuilding.secondaryWorkerAmount(secondaryWorkerAmount);

            float primaryWorkerPopTypeEmploymentRatio = (float) (currentPrimaryEmployed + primaryWorkerAmount) / Math.max(1f, demographics.totalByPopType(primaryWorkerPopTypeIndex));
            float secondaryWorkerPopTypeEmploymentRatio = (float) (currentSecondaryEmployed + secondaryWorkerAmount) / Math.max(1f, demographics.totalByPopType(secondaryWorkerPopTypeIndex));

            regionInstanceIncome.workersByPopType(primaryWorkerPopTypeIndex, regionInstanceIncome.workersByPopType(primaryWorkerPopTypeIndex) + primaryWorkerAmount);
            regionInstanceIncome.workersByPopType(secondaryWorkerPopTypeIndex, regionInstanceIncome.workersByPopType(secondaryWorkerPopTypeIndex) + secondaryWorkerAmount);
            regionInstanceData.workerPopTypeEmploymentRatios(primaryWorkerPopTypeIndex, primaryWorkerPopTypeEmploymentRatio);
            regionInstanceData.workerPopTypeEmploymentRatios(secondaryWorkerPopTypeIndex, secondaryWorkerPopTypeEmploymentRatio);
        }
    }
}

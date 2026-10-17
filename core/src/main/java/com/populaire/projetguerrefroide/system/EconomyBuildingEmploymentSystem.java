package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.EmploymentUtils.rationedWorkers;
import static com.populaire.projetguerrefroide.util.EmploymentUtils.workerTarget;

public class EconomyBuildingEmploymentSystem {

    public EconomyBuildingEmploymentSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("EconomyBuildingEmploymentSystem")
            .kind(phaseId)
            .with(EconomyBuilding.class)
            .with(Building.class)
            .with(EconomyBuildingType.class)
            .iter(this::hire);
    }

    private void hire(Iter iter) {
        long regionInstanceId = 0;
        RegionInstanceMutView regionInstanceData = null;
        RegionInstanceIncomeMutView regionInstanceIncome = null;
        RegionDemographicsView demographics = null;

        Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 0);
        Field<Building> buildingField = iter.field(Building.class, 1);
        Field<EconomyBuildingType> economyBuildingTypeField = iter.field(EconomyBuildingType.class, 2);
        EconomyBuildingTypeView economyBuildingType = economyBuildingTypeField.getView(0);
        int workforce = economyBuildingType.workforce();
        int primaryWorkerPopTypeIndex = economyBuildingType.primaryWorkerPopTypeIndex();
        int secondaryWorkerPopTypeIndex = economyBuildingType.secondaryWorkerPopTypeIndex();
        float primaryWorkerPopTypeRatio = economyBuildingType.primaryWorkerPopTypeRatio();
        float secondaryWorkerPopTypeRatio = economyBuildingType.secondaryWorkerPopTypeRatio();

        for(int i = 0; i < iter.count(); i++) {
            EconomyBuildingMutView economyBuilding = economyBuildingField.getMutView(i);
            BuildingView building = buildingField.getMutView(i);

            if(building.parentId() != regionInstanceId) {
                regionInstanceId = building.parentId();
                EntityView regionInstance = iter.world().obtainEntityView(regionInstanceId);
                regionInstanceData = regionInstance.getMutView(RegionInstance.class);
                regionInstanceIncome = regionInstance.getMutView(RegionInstanceIncome.class);
                demographics = regionInstance.getMutView(RegionDemographics.class);
            }

            int primaryTarget = workerTarget(workforce, building.size(), primaryWorkerPopTypeRatio, economyBuilding.scale());
            int secondaryTarget = workerTarget(workforce, building.size(), secondaryWorkerPopTypeRatio, economyBuilding.scale());

            int primaryTotal = demographics.totalByPopType(primaryWorkerPopTypeIndex);
            int secondaryTotal = demographics.totalByPopType(secondaryWorkerPopTypeIndex);

            int primaryWorkerAmount = rationedWorkers(primaryTarget, primaryTotal, regionInstanceIncome.workerDemandByPopType(primaryWorkerPopTypeIndex));
            int secondaryWorkerAmount = rationedWorkers(secondaryTarget, secondaryTotal, regionInstanceIncome.workerDemandByPopType(secondaryWorkerPopTypeIndex));

            economyBuilding.primaryWorkerAmount(primaryWorkerAmount);
            economyBuilding.secondaryWorkerAmount(secondaryWorkerAmount);

            regionInstanceIncome.workersByPopType(primaryWorkerPopTypeIndex, regionInstanceIncome.workersByPopType(primaryWorkerPopTypeIndex) + primaryWorkerAmount);
            regionInstanceIncome.workersByPopType(secondaryWorkerPopTypeIndex, regionInstanceIncome.workersByPopType(secondaryWorkerPopTypeIndex) + secondaryWorkerAmount);
            regionInstanceData.workerPopTypeEmploymentRatios(primaryWorkerPopTypeIndex, regionInstanceIncome.workersByPopType(primaryWorkerPopTypeIndex) / Math.max(1f, primaryTotal));
            regionInstanceData.workerPopTypeEmploymentRatios(secondaryWorkerPopTypeIndex, regionInstanceIncome.workersByPopType(secondaryWorkerPopTypeIndex) / Math.max(1f, secondaryTotal));
        }
    }
}

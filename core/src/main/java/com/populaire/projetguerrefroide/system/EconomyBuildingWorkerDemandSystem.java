package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.EmploymentUtils.workerTarget;

public class EconomyBuildingWorkerDemandSystem {

    public EconomyBuildingWorkerDemandSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("EconomyBuildingWorkerDemandSystem")
            .kind(phaseId)
            .with(EconomyBuilding.class)
            .with(Building.class)
            .with(EconomyBuildingType.class)
            .iter(this::demand);
    }

    private void demand(Iter iter) {
        long regionInstanceId = 0;
        RegionInstanceIncomeMutView regionInstanceIncome = null;

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
            EconomyBuildingView economyBuilding = economyBuildingField.getMutView(i);
            BuildingView building = buildingField.getMutView(i);

            if(building.parentId() != regionInstanceId) {
                regionInstanceId = building.parentId();
                regionInstanceIncome = iter.world().obtainEntityView(regionInstanceId).getMutView(RegionInstanceIncome.class);
            }

            int primaryTarget = workerTarget(workforce, building.size(), primaryWorkerPopTypeRatio, economyBuilding.scale());
            int secondaryTarget = workerTarget(workforce, building.size(), secondaryWorkerPopTypeRatio, economyBuilding.scale());

            regionInstanceIncome.workerDemandByPopType(primaryWorkerPopTypeIndex, regionInstanceIncome.workerDemandByPopType(primaryWorkerPopTypeIndex) + primaryTarget);
            regionInstanceIncome.workerDemandByPopType(secondaryWorkerPopTypeIndex, regionInstanceIncome.workerDemandByPopType(secondaryWorkerPopTypeIndex) + secondaryTarget);
        }
    }
}

package com.populaire.projetguerrefroide.system.building;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;

import static com.populaire.projetguerrefroide.system.population.EmploymentUtils.activeWorkers;
import static com.populaire.projetguerrefroide.system.population.EmploymentUtils.activeShare;
import static com.populaire.projetguerrefroide.system.population.EmploymentUtils.rationedWorkers;
import static com.populaire.projetguerrefroide.system.population.EmploymentUtils.workerTarget;

public class EconomyBuildingEmploymentSystem {

    private final long stateOwnerTag;
    private final EcsConstants ecsConstants;

    public EconomyBuildingEmploymentSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        this.stateOwnerTag = ecsConstants.countryTag();
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

        long countryId = 0;
        float primaryWorkforceShare = 0f;
        float secondaryWorkforceShare = 0f;

        Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 0);
        Field<Building> buildingField = iter.field(Building.class, 1);
        Field<EconomyBuildingType> economyBuildingTypeField = iter.field(EconomyBuildingType.class, 2);
        EconomyBuildingTypeView economyBuildingType = economyBuildingTypeField.getView(0);
        int workforce = economyBuildingType.workforce();
        int primaryWorkerPopTypeIndex = economyBuildingType.primaryWorkerPopTypeIndex();
        int secondaryWorkerPopTypeIndex = economyBuildingType.secondaryWorkerPopTypeIndex();
        float primaryWorkerPopTypeRatio = economyBuildingType.primaryWorkerPopTypeRatio();
        float secondaryWorkerPopTypeRatio = economyBuildingType.secondaryWorkerPopTypeRatio();
        float[] baseWorkforceShares = this.ecsConstants.baseWorkforceShares();

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

            if (building.countryId() != countryId) {
                countryId = building.countryId();
                CountryPopulationPolicyView populationPolicy = iter.world().obtainEntityView(countryId).getView(CountryPopulationPolicy.class);
                primaryWorkforceShare = activeShare(baseWorkforceShares[primaryWorkerPopTypeIndex], populationPolicy.workforceShareModifier());
                secondaryWorkforceShare = activeShare(baseWorkforceShares[secondaryWorkerPopTypeIndex], populationPolicy.workforceShareModifier());
            }

            int primaryTarget = workerTarget(workforce, building.size(), primaryWorkerPopTypeRatio, economyBuilding.utilization());
            int secondaryTarget = workerTarget(workforce, building.size(), secondaryWorkerPopTypeRatio, economyBuilding.utilization());

            int primaryTotal = demographics.totalByPopType(primaryWorkerPopTypeIndex);
            int secondaryTotal = demographics.totalByPopType(secondaryWorkerPopTypeIndex);

            int primaryWorkerAmount = rationedWorkers(primaryTarget, activeWorkers(primaryTotal, primaryWorkforceShare), regionInstanceIncome.workerDemandByPopType(primaryWorkerPopTypeIndex));
            int secondaryWorkerAmount = rationedWorkers(secondaryTarget, activeWorkers(secondaryTotal, secondaryWorkforceShare), regionInstanceIncome.workerDemandByPopType(secondaryWorkerPopTypeIndex));

            economyBuilding.primaryWorkerAmount(primaryWorkerAmount);
            economyBuilding.secondaryWorkerAmount(secondaryWorkerAmount);

            regionInstanceIncome.workersByPopType(primaryWorkerPopTypeIndex, regionInstanceIncome.workersByPopType(primaryWorkerPopTypeIndex) + primaryWorkerAmount);
            regionInstanceIncome.workersByPopType(secondaryWorkerPopTypeIndex, regionInstanceIncome.workersByPopType(secondaryWorkerPopTypeIndex) + secondaryWorkerAmount);
            if (economyBuilding.ownerTagId() == this.stateOwnerTag) {
                regionInstanceIncome.stateBuildingWorkers(regionInstanceIncome.stateBuildingWorkers() + primaryWorkerAmount + secondaryWorkerAmount);
            }
            regionInstanceData.workerPopTypeEmploymentRatios(primaryWorkerPopTypeIndex, regionInstanceIncome.workersByPopType(primaryWorkerPopTypeIndex) / Math.max(1f, primaryTotal));
            regionInstanceData.workerPopTypeEmploymentRatios(secondaryWorkerPopTypeIndex, regionInstanceIncome.workersByPopType(secondaryWorkerPopTypeIndex) / Math.max(1f, secondaryTotal));
        }
    }
}

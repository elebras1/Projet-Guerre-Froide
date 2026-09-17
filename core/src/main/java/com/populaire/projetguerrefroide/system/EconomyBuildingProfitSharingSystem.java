package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;

public class EconomyBuildingProfitSharingSystem {

    private final EcsConstants ecsConstants;

    public EconomyBuildingProfitSharingSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("EconomyBuildingProfitSharingSystem")
            .kind(phaseId)
            .with(Building.class)
            .with(EconomyBuilding.class)
            .iter(this::process);
    }

    private void process(Iter iter) {
        EntityView globalPopType = iter.world().obtainEntityView(iter.world().lookup("global_population_type"));
        GlobalPopulationTypeView globalPopTypeData = globalPopType.getMutView(GlobalPopulationType.class);
        int capitalistPopTypeIndex = globalPopTypeData.capitalistPopTypeIndex();

        long regionId = 0;
        RegionInstanceIncomeView regionIncome = null;
        DemographicsView regionDemographics = null;
        long countryId = 0;
        CountryProfitDistributionPolicyView countryProfitDistributionPolicy = null;
        long buildingTypeId = 0;
        EconomyBuildingTypeView buildingTypeData = null;

        Field<Building> buildingField = iter.field(Building.class, 0);
        Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 1);
        for(int i = 0; i < iter.count(); i++) {
            BuildingView building = buildingField.getMutView(i);
            EconomyBuildingView economyBuilding = economyBuildingField.getMutView(i);

            if(building.parentId() != regionId) {
                regionId = building.parentId();
                EntityView region = iter.world().obtainEntityView(regionId);
                regionIncome = region.getMutView(RegionInstanceIncome.class);
                regionDemographics = region.getMutView(Demographics.class);
            }

            if(building.typeId() != buildingTypeId) {
                buildingTypeId = building.typeId();
                EntityView buildingType = iter.world().obtainEntityView(buildingTypeId);
                buildingTypeData = buildingType.getMutView(EconomyBuildingType.class);
            }

            if(building.countryId() != countryId) {
                countryId = building.countryId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryProfitDistributionPolicy = country.getMutView(CountryProfitDistributionPolicy.class);
            }

            int primaryWorkerPopTypeIndex = buildingTypeData.primaryWorkerPopTypeIndex();
            int secondaryWorkerPopTypeIndex = buildingTypeData.secondaryWorkerPopTypeIndex();
            regionIncome.minWagesByPopType(primaryWorkerPopTypeIndex, regionIncome.minWagesByPopType(primaryWorkerPopTypeIndex) + economyBuilding.primaryWorkerMinWage());
            regionIncome.minWagesByPopType(secondaryWorkerPopTypeIndex, regionIncome.minWagesByPopType(secondaryWorkerPopTypeIndex) + economyBuilding.secondaryWorkerMinWage());
            regionIncome.workersByPopType(primaryWorkerPopTypeIndex, regionIncome.workersByPopType(primaryWorkerPopTypeIndex) + economyBuilding.primaryWorkerAmount());
            regionIncome.workersByPopType(secondaryWorkerPopTypeIndex, regionIncome.workersByPopType(secondaryWorkerPopTypeIndex) + economyBuilding.secondaryWorkerAmount());

            if(economyBuilding.profit() <= 0f) {
                continue;
            }

            float capitalistShareRatio = countryProfitDistributionPolicy.capitalistProfitShareRate();
            float workerShareRatio = countryProfitDistributionPolicy.workerProfitShareRate();
            float stateShareRatio = countryProfitDistributionPolicy.stateProfitShareRate();

            if(economyBuilding.ownerTagId() == this.ecsConstants.countryTag()) {
                float totalShareStateRatio = stateShareRatio + workerShareRatio;
                if (totalShareStateRatio > 0f) {
                    float scalingFactor = 1f / totalShareStateRatio;
                    stateShareRatio *= scalingFactor;
                    workerShareRatio *= scalingFactor;
                } else {
                    stateShareRatio = 1f;
                }
                regionIncome.countryProfitShare(regionIncome.countryProfitShare() + stateShareRatio * economyBuilding.profit());
            } else {
                float totalShareCapitalist = capitalistShareRatio + workerShareRatio;
                if(totalShareCapitalist > 0f) {
                    float scalingFactor = 1f / totalShareCapitalist;
                    capitalistShareRatio *= scalingFactor;
                    workerShareRatio *= scalingFactor;
                } else {
                    capitalistShareRatio = 1f;
                }
                float capitalistProfitShare = capitalistShareRatio * economyBuilding.profit();
                if(capitalistPopTypeIndex >= 0 && regionDemographics.totalByPopType(capitalistPopTypeIndex) > 0) {
                    regionIncome.capitalistProfitShare(regionIncome.capitalistProfitShare() + capitalistProfitShare);
                } else {
                    regionIncome.countryProfitShare(regionIncome.countryProfitShare() + capitalistProfitShare);
                }
            }

            float workerShare = workerShareRatio * economyBuilding.profit();

            float primaryWorkerShare = workerShare / 3.0f;
            float secondaryWorkerShare = workerShare - primaryWorkerShare;

            if(regionDemographics.totalByPopType(primaryWorkerPopTypeIndex) > 0) {
                regionIncome.profitShareByPopType(primaryWorkerPopTypeIndex, regionIncome.profitShareByPopType(primaryWorkerPopTypeIndex) + primaryWorkerShare);
            } else {
                regionIncome.countryProfitShare(regionIncome.countryProfitShare() + primaryWorkerShare);
            }

            if(regionDemographics.totalByPopType(secondaryWorkerPopTypeIndex) > 0) {
                regionIncome.profitShareByPopType(secondaryWorkerPopTypeIndex, regionIncome.profitShareByPopType(secondaryWorkerPopTypeIndex) + secondaryWorkerShare);
            } else {
                regionIncome.countryProfitShare(regionIncome.countryProfitShare() + secondaryWorkerShare);
            }
        }

    }
}

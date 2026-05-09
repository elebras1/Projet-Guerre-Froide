package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
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
        long regionId = 0;
        RegionInstanceIncomeView regionIncome = null;
        long countryId = 0;
        CountryEffectPolicyView countryEffectPolicy = null;
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
            }

            if(building.typeId() != buildingTypeId) {
                buildingTypeId = building.typeId();
                EntityView buildingType = iter.world().obtainEntityView(buildingTypeId);
                buildingTypeData = buildingType.getMutView(EconomyBuildingType.class);
            }

            if(building.countryId() != countryId) {
                countryId = building.countryId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryEffectPolicy = country.getMutView(CountryEffectPolicy.class);
            }

            int primaryWorkerPopTypeIndex = buildingTypeData.primaryWorkerPopTypeIndex();
            int secondaryWorkerPopTypeIndex = buildingTypeData.secondaryWorkerPopTypeIndex();
            regionIncome.minWagesByPopType(primaryWorkerPopTypeIndex, regionIncome.minWagesByPopType(primaryWorkerPopTypeIndex) + economyBuilding.primaryWorkerMinWage());
            regionIncome.minWagesByPopType(secondaryWorkerPopTypeIndex, regionIncome.minWagesByPopType(secondaryWorkerPopTypeIndex) + economyBuilding.secondaryWorkerMinWage());

            if(economyBuilding.profit() <= 0f) {
                continue;
            }

            float capitalistShareRatio = countryEffectPolicy.capitalistProfitShareRate();
            float workerShareRatio = countryEffectPolicy.workerProfitShareRate();
            float stateShareRatio = countryEffectPolicy.stateProfitShareRate();

            if(economyBuilding.ownerTagId() == this.ecsConstants.countryTag()) {
                float totalShareStateRatio = stateShareRatio + workerShareRatio;
                if (totalShareStateRatio > 1f) {
                    float scalingFactor = 1f / totalShareStateRatio;
                    stateShareRatio *= scalingFactor;
                    workerShareRatio *= scalingFactor;
                }
                regionIncome.countryProfitShare(regionIncome.countryProfitShare() + stateShareRatio * economyBuilding.profit());
            } else {
                float totalShareCapitalist = capitalistShareRatio + workerShareRatio;
                if(totalShareCapitalist > 1f) {
                    float scalingFactor = 1f / totalShareCapitalist;
                    capitalistShareRatio *= scalingFactor;
                    workerShareRatio *= scalingFactor;
                }
                regionIncome.capitalistProfitShare(regionIncome.capitalistProfitShare() + capitalistShareRatio  * economyBuilding.profit());
            }

            float workerShare = workerShareRatio * economyBuilding.profit();

            float primaryWorkerShare = workerShare / 3.0f;
            float secondaryWorkerShare = workerShare - primaryWorkerShare;

            regionIncome.profitShareByPopType(primaryWorkerPopTypeIndex, regionIncome.profitShareByPopType(primaryWorkerPopTypeIndex) + primaryWorkerShare);
            regionIncome.profitShareByPopType(secondaryWorkerPopTypeIndex, regionIncome.profitShareByPopType(secondaryWorkerPopTypeIndex) + secondaryWorkerShare);
            regionIncome.workersByPopType(primaryWorkerPopTypeIndex, regionIncome.workersByPopType(primaryWorkerPopTypeIndex) + economyBuilding.primaryWorkerAmount());
            regionIncome.workersByPopType(secondaryWorkerPopTypeIndex, regionIncome.workersByPopType(secondaryWorkerPopTypeIndex) + economyBuilding.secondaryWorkerAmount());
        }

    }
}

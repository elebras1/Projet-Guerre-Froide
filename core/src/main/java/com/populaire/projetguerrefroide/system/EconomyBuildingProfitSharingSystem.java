package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.BankUtils;
import com.populaire.projetguerrefroide.util.EcsConstants;

import static com.populaire.projetguerrefroide.util.Constants.CAPITALIST_OWNER;
import static com.populaire.projetguerrefroide.util.Constants.COOPERATIVE_RETAINED_SHARE;
import static com.populaire.projetguerrefroide.util.Constants.WORKERS_OWNER;

public class EconomyBuildingProfitSharingSystem {

    private final EcsConstants ecsConstants;

    public EconomyBuildingProfitSharingSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("EconomyBuildingProfitSharingSystem")
            .kind(phaseId)
            .with(Building.class)
            .with(EconomyBuilding.class)
            .with(EconomyBuildingType.class)
            .iter(this::process);
    }

    private void process(Iter iter) {
        EntityView globalPopType = iter.world().obtainEntityView(iter.world().lookup("global_population_type"));
        GlobalPopulationTypeView globalPopTypeData = globalPopType.getMutView(GlobalPopulationType.class);
        int capitalistPopTypeIndex = globalPopTypeData.capitalistPopTypeIndex();

        long regionId = 0;
        RegionInstanceView regionInstance = null;
        RegionInstanceIncomeMutView regionIncome = null;
        RegionDemographicsView regionDemographics = null;
        long countryId = 0;
        CountryProfitDistributionPolicyView countryProfitDistributionPolicy = null;
        CountryInvestmentMutView countryInvestment = null;
        CountryTaxPolicyView countryTaxPolicy = null;
        CountryMarketMutView countryMarket = null;
        CountryBankMutView countryBank = null;

        Field<Building> buildingField = iter.field(Building.class, 0);
        Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 1);
        Field<EconomyBuildingType> buildingTypeField = iter.field(EconomyBuildingType.class, 2);
        EconomyBuildingTypeView buildingTypeData = buildingTypeField.getView(0);
        for(int i = 0; i < iter.count(); i++) {
            BuildingView building = buildingField.getMutView(i);
            EconomyBuildingView economyBuilding = economyBuildingField.getMutView(i);

            if(building.parentId() != regionId) {
                regionId = building.parentId();
                EntityView region = iter.world().obtainEntityView(regionId);
                regionInstance = region.getView(RegionInstance.class);
                regionIncome = region.getMutView(RegionInstanceIncome.class);
                regionDemographics = region.getMutView(RegionDemographics.class);
            }

            if(building.countryId() != countryId) {
                countryId = building.countryId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryProfitDistributionPolicy = country.getMutView(CountryProfitDistributionPolicy.class);
                countryInvestment = country.getMutView(CountryInvestment.class);
                countryTaxPolicy = country.getView(CountryTaxPolicy.class);
                countryMarket = country.getMutView(CountryMarket.class);
                countryBank = country.getMutView(CountryBank.class);
            }

            int primaryWorkerPopTypeIndex = buildingTypeData.primaryWorkerPopTypeIndex();
            int secondaryWorkerPopTypeIndex = buildingTypeData.secondaryWorkerPopTypeIndex();
            regionIncome.minWagesByPopType(primaryWorkerPopTypeIndex, regionIncome.minWagesByPopType(primaryWorkerPopTypeIndex) + economyBuilding.primaryWorkerMinWage());
            regionIncome.minWagesByPopType(secondaryWorkerPopTypeIndex, regionIncome.minWagesByPopType(secondaryWorkerPopTypeIndex) + economyBuilding.secondaryWorkerMinWage());
            regionIncome.workersByPopType(primaryWorkerPopTypeIndex, regionIncome.workersByPopType(primaryWorkerPopTypeIndex) + economyBuilding.primaryWorkerAmount());
            regionIncome.workersByPopType(secondaryWorkerPopTypeIndex, regionIncome.workersByPopType(secondaryWorkerPopTypeIndex) + economyBuilding.secondaryWorkerAmount());

            float dividend = economyBuilding.dividend();
            if(dividend <= 0f) {
                continue;
            }

            float capitalistShareRatio = countryProfitDistributionPolicy.capitalistProfitShareRate();
            float workerShareRatio = countryProfitDistributionPolicy.workerProfitShareRate();
            float stateShareRatio = countryProfitDistributionPolicy.stateProfitShareRate();

            boolean workersOwned = economyBuilding.ownerTagId() == this.ecsConstants.workersTag();
            if(economyBuilding.ownerTagId() == this.ecsConstants.countryTag() || workersOwned) {
                float totalShareStateRatio = stateShareRatio + workerShareRatio;
                if (totalShareStateRatio > 0f) {
                    float scalingFactor = 1f / totalShareStateRatio;
                    stateShareRatio *= scalingFactor;
                    workerShareRatio *= scalingFactor;
                } else {
                    stateShareRatio = 1f;
                }
                regionIncome.countryProfitShare(regionIncome.countryProfitShare() + stateShareRatio * dividend);
            } else {
                float profitTax = countryTaxPolicy.profitTaxRate() * regionInstance.taxCollectionShare() * dividend;
                countryMarket.treasury(countryMarket.treasury() + profitTax);
                countryMarket.taxRevenue(countryMarket.taxRevenue() + profitTax);
                dividend -= profitTax;
                float totalShareCapitalist = capitalistShareRatio + workerShareRatio;
                if(totalShareCapitalist > 0f) {
                    float scalingFactor = 1f / totalShareCapitalist;
                    capitalistShareRatio *= scalingFactor;
                    workerShareRatio *= scalingFactor;
                } else {
                    capitalistShareRatio = 1f;
                }
                float capitalistProfitShare = BankUtils.withholdDebtService(countryBank, countryInvestment, CAPITALIST_OWNER, capitalistShareRatio * dividend);
                if(capitalistPopTypeIndex >= 0 && regionDemographics.totalByPopType(capitalistPopTypeIndex) > 0) {
                    regionIncome.capitalistProfitShare(regionIncome.capitalistProfitShare() + capitalistProfitShare);
                } else {
                    regionIncome.countryProfitShare(regionIncome.countryProfitShare() + capitalistProfitShare);
                }
            }

            float workerShare = workerShareRatio * dividend;
            if (workersOwned) {
                workerShare = BankUtils.withholdDebtService(countryBank, countryInvestment, WORKERS_OWNER, workerShare);
                float retainedShare = COOPERATIVE_RETAINED_SHARE * workerShare;
                countryInvestment.funds(WORKERS_OWNER, countryInvestment.funds(WORKERS_OWNER) + retainedShare);
                workerShare -= retainedShare;
            }

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

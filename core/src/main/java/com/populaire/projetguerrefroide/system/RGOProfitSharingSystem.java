package com.populaire.projetguerrefroide.system;

import com.populaire.projetguerrefroide.util.BankUtils;
import com.populaire.projetguerrefroide.util.EcsConstants;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.Constants.CAPITALIST_OWNER;

public class RGOProfitSharingSystem {

    public RGOProfitSharingSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        ecsWorld.system("RGOProfitSharingSystem")
            .kind(phaseId)
            .with(Province.class)
            .with(ResourceGathering.class)
            .with(ResourceGatheringType.class).up(ecsConstants.rgoType())
            .iter(this::process);
    }

    private void process(Iter iter) {
        EntityView globalPopType = iter.world().obtainEntityView(iter.world().lookup("global_population_type"));
        GlobalPopulationTypeView globalPopTypeData = globalPopType.getMutView(GlobalPopulationType.class);
        int capitalistPopTypeIndex = globalPopTypeData.capitalistPopTypeIndex();
        int aristocratPopTypeIndex = globalPopTypeData.aristocratPopTypeIndex();

        long regionId = 0;
        RegionInstanceView regionInstance = null;
        RegionInstanceIncomeMutView regionIncome = null;
        RegionDemographicsView regionDemographics = null;
        long countryId = 0;
        CountryProfitDistributionPolicyView countryProfitDistributionPolicy = null;
        CountryTaxPolicyView countryTaxPolicy = null;
        CountryMarketMutView countryMarket = null;
        CountryBankMutView countryBank = null;
        CountryInvestmentMutView countryInvestment = null;

        Field<Province> provinceField = iter.field(Province.class, 0);
        Field<ResourceGathering> resourceGatheringField = iter.field(ResourceGathering.class, 1);
        int workerPopTypeIndex = iter.field(ResourceGatheringType.class, 2).<ResourceGatheringTypeView>getView(0).workerPopTypeIndex();
        for(int i = 0; i < iter.count(); i++) {
            ProvinceView province = provinceField.getMutView(i);
            ResourceGatheringView resourceGathering = resourceGatheringField.getMutView(i);

            if(province.regionInstanceId() != regionId) {
                regionId = province.regionInstanceId();
                EntityView region = iter.world().obtainEntityView(regionId);
                regionInstance = region.getView(RegionInstance.class);
                regionIncome = region.getMutView(RegionInstanceIncome.class);
                regionDemographics = region.getMutView(RegionDemographics.class);
            }


            if(province.ownerId() != countryId) {
                countryId = province.ownerId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryProfitDistributionPolicy = country.getMutView(CountryProfitDistributionPolicy.class);
                countryTaxPolicy = country.getView(CountryTaxPolicy.class);
                countryMarket = country.getMutView(CountryMarket.class);
                countryBank = country.getMutView(CountryBank.class);
                countryInvestment = country.getMutView(CountryInvestment.class);
            }

            regionIncome.minWagesByPopType(workerPopTypeIndex, regionIncome.minWagesByPopType(workerPopTypeIndex) + resourceGathering.workerMinWage());
            regionIncome.workersByPopType(workerPopTypeIndex, regionIncome.workersByPopType(workerPopTypeIndex) + resourceGathering.workerAmount());

            float profit = resourceGathering.profit();
            if(profit <= 0f) {
                continue;
            }

            float stateShareRatio = countryProfitDistributionPolicy.stateProfitShareRate();
            float capitalistShareRatio = countryProfitDistributionPolicy.capitalistProfitShareRate();
            float aristocratShareRatio = countryProfitDistributionPolicy.aristocratProfitShareRate();
            float workerShareRatio = countryProfitDistributionPolicy.workerProfitShareRate();

            if (capitalistShareRatio > 0f) {
                float resourceTax = countryTaxPolicy.resourceTaxRate() * regionInstance.taxCollectionShare() * profit;
                countryMarket.treasury(countryMarket.treasury() + resourceTax);
                countryMarket.taxRevenue(countryMarket.taxRevenue() + resourceTax);
                profit -= resourceTax;
                stateShareRatio = 0f;
            }

            float totalShareRatio = stateShareRatio + capitalistShareRatio + aristocratShareRatio + workerShareRatio;
            if (totalShareRatio <= 0f) {
                regionIncome.countryProfitShare(regionIncome.countryProfitShare() + profit);
                continue;
            }

            float scalingFactor = 1f / totalShareRatio;
            stateShareRatio *= scalingFactor;
            capitalistShareRatio *= scalingFactor;
            aristocratShareRatio *= scalingFactor;
            workerShareRatio *= scalingFactor;

            regionIncome.countryProfitShare(regionIncome.countryProfitShare() + stateShareRatio * profit);

            float capitalistProfitShare = BankUtils.withholdDebtService(countryBank, countryInvestment, CAPITALIST_OWNER, capitalistShareRatio * profit);
            if(capitalistPopTypeIndex >= 0 && regionDemographics.totalByPopType(capitalistPopTypeIndex) > 0) {
                regionIncome.capitalistProfitShare(regionIncome.capitalistProfitShare() + capitalistProfitShare);
            } else {
                regionIncome.countryProfitShare(regionIncome.countryProfitShare() + capitalistProfitShare);
            }

            float aristocratProfitShare = aristocratShareRatio * profit;
            if(aristocratPopTypeIndex >= 0 && regionDemographics.totalByPopType(aristocratPopTypeIndex) > 0) {
                regionIncome.aristocratProfitShare(regionIncome.aristocratProfitShare() + aristocratProfitShare);
            } else {
                regionIncome.countryProfitShare(regionIncome.countryProfitShare() + aristocratProfitShare);
            }

            float workerShare = workerShareRatio * profit;
            if(regionDemographics.totalByPopType(workerPopTypeIndex) > 0) {
                regionIncome.profitShareByPopType(workerPopTypeIndex, regionIncome.profitShareByPopType(workerPopTypeIndex) + workerShare);
            } else {
                regionIncome.countryProfitShare(regionIncome.countryProfitShare() + workerShare);
            }
        }
    }
}

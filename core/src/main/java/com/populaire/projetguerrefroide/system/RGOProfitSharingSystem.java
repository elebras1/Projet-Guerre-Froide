package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class RGOProfitSharingSystem {

    public RGOProfitSharingSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("RGOProfitSharingSystem")
            .kind(phaseId)
            .with(Province.class)
            .with(ResourceGathering.class)
            .multiThreaded()
            .iter(this::process);
    }

    private void process(Iter iter) {
        EntityView globalPopType = iter.world().obtainEntityView(iter.world().lookup("global_population_type"));
        GlobalPopulationTypeView globalPopTypeData = globalPopType.getMutView(GlobalPopulationType.class);
        int capitalistPopTypeIndex = globalPopTypeData.capitalistPopTypeIndex();
        int aristocratPopTypeIndex = globalPopTypeData.aristocratPopTypeIndex();

        long regionId = 0;
        RegionInstanceIncomeView regionIncome = null;
        DemographicsView regionDemographics = null;
        long countryId = 0;
        CountryProfitDistributionPolicyView countryProfitDistributionPolicy = null;
        long rgoTypeId = 0;
        ResourceGatheringTypeView rgoTypeData = null;

        Field<Province> provinceField = iter.field(Province.class, 0);
        Field<ResourceGathering> resourceGatheringField = iter.field(ResourceGathering.class, 1);
        for(int i = 0; i < iter.count(); i++) {
            ProvinceView province = provinceField.getMutView(i);
            ResourceGatheringView resourceGathering = resourceGatheringField.getMutView(i);

            if(province.regionInstanceId() != regionId) {
                regionId = province.regionInstanceId();
                EntityView region = iter.world().obtainEntityView(regionId);
                regionIncome = region.getMutView(RegionInstanceIncome.class);
                regionDemographics = region.getMutView(Demographics.class);
            }

            if(resourceGathering.typeId() != rgoTypeId) {
                rgoTypeId = resourceGathering.typeId();
                EntityView rgoType = iter.world().obtainEntityView(rgoTypeId);
                rgoTypeData = rgoType.getMutView(ResourceGatheringType.class);
            }

            if(province.ownerId() != countryId) {
                countryId = province.ownerId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryProfitDistributionPolicy = country.getMutView(CountryProfitDistributionPolicy.class);
            }

            int workerPopTypeIndex = rgoTypeData.workerPopTypeIndex();
            regionIncome.minWagesByPopType(workerPopTypeIndex, regionIncome.minWagesByPopType(workerPopTypeIndex) + resourceGathering.workerMinWage());
            regionIncome.workersByPopType(workerPopTypeIndex, regionIncome.workersByPopType(workerPopTypeIndex) + resourceGathering.workerAmount());

            if(resourceGathering.profit() <= 0f) {
                continue;
            }

            float stateShareRatio = countryProfitDistributionPolicy.stateProfitShareRate();
            float capitalistShareRatio = countryProfitDistributionPolicy.capitalistProfitShareRate();
            float aristocratShareRatio = countryProfitDistributionPolicy.aristocratProfitShareRate();
            float workerShareRatio = countryProfitDistributionPolicy.workerProfitShareRate();

            float totalShareRatio = stateShareRatio + capitalistShareRatio + aristocratShareRatio + workerShareRatio;
            if (totalShareRatio <= 0f) {
                regionIncome.countryProfitShare(regionIncome.countryProfitShare() + resourceGathering.profit());
                continue;
            }

            float scalingFactor = 1f / totalShareRatio;
            stateShareRatio *= scalingFactor;
            capitalistShareRatio *= scalingFactor;
            aristocratShareRatio *= scalingFactor;
            workerShareRatio *= scalingFactor;

            regionIncome.countryProfitShare(regionIncome.countryProfitShare() + stateShareRatio * resourceGathering.profit());

            float capitalistProfitShare = capitalistShareRatio * resourceGathering.profit();
            if(capitalistPopTypeIndex >= 0 && regionDemographics.totalByPopType(capitalistPopTypeIndex) > 0) {
                regionIncome.capitalistProfitShare(regionIncome.capitalistProfitShare() + capitalistProfitShare);
            } else {
                regionIncome.countryProfitShare(regionIncome.countryProfitShare() + capitalistProfitShare);
            }

            float aristocratProfitShare = aristocratShareRatio * resourceGathering.profit();
            if(aristocratPopTypeIndex >= 0 && regionDemographics.totalByPopType(aristocratPopTypeIndex) > 0) {
                regionIncome.aristocratProfitShare(regionIncome.aristocratProfitShare() + aristocratProfitShare);
            } else {
                regionIncome.countryProfitShare(regionIncome.countryProfitShare() + aristocratProfitShare);
            }

            float workerShare = workerShareRatio * resourceGathering.profit();
            if(regionDemographics.totalByPopType(workerPopTypeIndex) > 0) {
                regionIncome.profitShareByPopType(workerPopTypeIndex, regionIncome.profitShareByPopType(workerPopTypeIndex) + workerShare);
            } else {
                regionIncome.countryProfitShare(regionIncome.countryProfitShare() + workerShare);
            }
        }
    }
}

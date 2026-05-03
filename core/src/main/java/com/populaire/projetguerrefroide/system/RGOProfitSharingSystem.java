package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class RGOProfitSharingSystem {

    public RGOProfitSharingSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("RGOProfitSharingSystem")
            .kind(phaseId)
            .with(Province.class)
            .with(ResourceGathering.class)
            .iter(this::process);
    }

    private void process(Iter iter) {
        long regionId = 0;
        RegionInstanceIncomeView regionIncome = null;
        long countryId = 0;
        CountryEffectPolicyView countryEffectPolicy = null;
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
            }

            if(resourceGathering.typeId() != rgoTypeId) {
                rgoTypeId = resourceGathering.typeId();
                EntityView rgoType = iter.world().obtainEntityView(rgoTypeId);
                rgoTypeData = rgoType.getMutView(ResourceGatheringType.class);
            }

            if(province.ownerId() != countryId) {
                countryId = province.ownerId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryEffectPolicy = country.getMutView(CountryEffectPolicy.class);
            }

            int workerPopTypeIndex = rgoTypeData.workerPopTypeIndex();
            regionIncome.minWagesByPopType(workerPopTypeIndex, regionIncome.minWagesByPopType(workerPopTypeIndex) + resourceGathering.workerMinWage());

            if(resourceGathering.profit() <= 0f) {
                continue;
            }

            float aristocratShareRatio = countryEffectPolicy.aristocratProfitShare();
            float workerShareRatio = countryEffectPolicy.workerProfitShare();
            float stateShareRatio = countryEffectPolicy.stateProfitShare();

            if(aristocratShareRatio <= 0f) {
                float totalShareStateRatio = stateShareRatio + workerShareRatio;
                if (totalShareStateRatio > 1f) {
                    float scalingFactor = 1f / totalShareStateRatio;
                    stateShareRatio *= scalingFactor;
                    workerShareRatio *= scalingFactor;
                }
                regionIncome.countryProfitShare(regionIncome.countryProfitShare() + stateShareRatio * resourceGathering.profit());
            } else {
                float totalShareAristocrat = aristocratShareRatio + workerShareRatio;
                if(totalShareAristocrat > 1f) {
                    float scalingFactor = 1f / totalShareAristocrat;
                    aristocratShareRatio *= scalingFactor;
                    workerShareRatio *= scalingFactor;
                }
                regionIncome.aristocratProfitShare(regionIncome.aristocratProfitShare() + aristocratShareRatio  * resourceGathering.profit());
            }

            float workerShare = workerShareRatio * resourceGathering.profit();
            regionIncome.profitShareByPopType(workerPopTypeIndex, regionIncome.profitShareByPopType(workerPopTypeIndex) + workerShare);
            regionIncome.workersByPopType(workerPopTypeIndex, regionIncome.workersByPopType(workerPopTypeIndex) + resourceGathering.workerAmount());
        }
    }
}

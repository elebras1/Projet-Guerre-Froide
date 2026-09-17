package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.Constants.NEEDS_SCALING_FACTOR;

public class RGORevenueSystem {

    public RGORevenueSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("RGORevenueSystem")
            .kind(phaseId)
            .with(Province.class)
            .with(ResourceGathering.class)
            .iter(this::distribute);
    }

    private void distribute(Iter iter) {
        long countryId = 0;
        CountryMarketView countryMarket = null;
        CountryLaborPolicyView countryLaborPolicy = null;

        long resourceGatheringTypeId = 0;
        ResourceGatheringTypeView resourceGatheringTypeData = null;

        Field<Province> provinceField = iter.field(Province.class, 0);
        Field<ResourceGathering> resourceGatheringField = iter.field(ResourceGathering.class, 1);

        for(int i = 0; i < iter.count(); i++) {
            ProvinceView province = provinceField.getMutView(i);
            ResourceGatheringView resourceGathering = resourceGatheringField.getMutView(i);

            if(province.ownerId() != countryId) {
                countryId = province.ownerId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
                countryLaborPolicy = country.getMutView(CountryLaborPolicy.class);
            }

            if(resourceGathering.typeId() != resourceGatheringTypeId) {
                resourceGatheringTypeId = resourceGathering.typeId();
                EntityView resourceGatheringType = iter.world().obtainEntityView(resourceGatheringTypeId);
                resourceGatheringTypeData = resourceGatheringType.getMutView(ResourceGatheringType.class);
            }

            float productionValue = countryMarket.productionValue();
            float value = resourceGathering.production() * countryMarket.goodPrices(resourceGathering.goodIndex());
            float revenue = productionValue > 0f ? countryMarket.salesRevenue() * value / productionValue : 0f;

            float workerMinWageFactor = (countryMarket.lifeCostsByPopType(resourceGatheringTypeData.workerPopTypeIndex()) + 0.2f * countryMarket.everydayCostsByPopType(resourceGatheringTypeData.workerPopTypeIndex())) * (1f + countryLaborPolicy.minWageFactor());
            float normalizedWages = workerMinWageFactor * resourceGathering.workerAmount() / NEEDS_SCALING_FACTOR;

            float wage = Math.min(normalizedWages, revenue);
            resourceGathering.workerMinWage(wage);
            resourceGathering.profit(revenue - wage);
        }
    }
}

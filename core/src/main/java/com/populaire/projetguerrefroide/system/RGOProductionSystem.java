package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class RGOProductionSystem {

    public RGOProductionSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("RGOProductionSystem")
            .kind(phaseId)
            .with(Province.class)
            .with(ResourceGathering.class)
            .iter(this::produce);
    }

    private void produce(Iter iter) {
        long countryId = 0;
        CountryMarketView countryMarket = null;
        CountryEffectPolicyView countryEffectPolicy = null;

        long resourceGatheringTypeId = 0;
        ResourceGatheringTypeView resourceGatheringTypeData = null;

        Field<Province> provinceField = iter.field(Province.class, 0);
        Field<ResourceGathering> resourceGatheringField = iter.field(ResourceGathering.class, 1);

        for(int i = 0; i < iter.count(); i++) {
            ProvinceView province = provinceField.getMutView(i);
            ResourceGatheringView resourceGathering = resourceGatheringField.getMutView(i);

            if(resourceGathering.typeId() != resourceGatheringTypeId) {
                resourceGatheringTypeId = resourceGathering.typeId();
                EntityView resourceGatheringType = iter.world().obtainEntityView(resourceGatheringTypeId);
                resourceGatheringTypeData = resourceGatheringType.getMutView(ResourceGatheringType.class);
            }

            if(province.ownerId() != countryId) {
                countryId = province.ownerId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
                countryEffectPolicy = country.getMutView(CountryEffectPolicy.class);
            }

            int maxCapacity = resourceGathering.size() * resourceGatheringTypeData.workforce();
            float baseProduction = resourceGathering.size() * resourceGathering.goodAmount();

            int targetWorkers = (int) (maxCapacity * resourceGatheringTypeData.workerPopTypeRatio());
            int targetSlaves = (int) (maxCapacity * resourceGatheringTypeData.slavePopTypeRatio());

            float workerFulfillment = (float) resourceGathering.workerAmount() / Math.max(1, targetWorkers);
            float slaveFulfillment = (float) resourceGathering.slaveAmount() / Math.max(1, targetSlaves);

            float coreProduction = workerFulfillment * baseProduction * resourceGatheringTypeData.workerEffectMultiplier();

            float maxBonus = resourceGatheringTypeData.slaveEffectMultiplier() - 1.0f;
            float currentSlaveBonus = 1.0f + (slaveFulfillment * maxBonus);

            float production = coreProduction * currentSlaveBonus;
            resourceGathering.production(production);

            float workerMinWageFactor = (countryMarket.lifeCostsByPopType(resourceGatheringTypeData.workerPopTypeIndex()) + 0.2f * countryMarket.everydayCostsByPopType(resourceGatheringTypeData.workerPopTypeIndex())) * (1f + countryEffectPolicy.minWageFactor());
            float workerMinWage = workerMinWageFactor * resourceGathering.workerAmount();
            float revenue = production * countryMarket.goodPrices(resourceGathering.goodIndex());
            if(revenue < workerMinWage) {
                float scalingFactor = revenue / workerMinWage;
                workerMinWage *= scalingFactor;
                resourceGathering.profit(0f);
            } else {
                resourceGathering.profit(revenue - workerMinWage);
            }
            resourceGathering.workerMinWage(workerMinWage);
        }
    }
}

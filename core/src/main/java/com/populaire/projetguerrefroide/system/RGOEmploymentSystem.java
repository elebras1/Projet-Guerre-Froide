package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;
import com.populaire.projetguerrefroide.util.EmploymentUtils;

import static com.populaire.projetguerrefroide.util.Constants.*;
import static com.populaire.projetguerrefroide.util.EmploymentUtils.activeWorkers;
import static com.populaire.projetguerrefroide.util.EmploymentUtils.activeShare;

public class RGOEmploymentSystem {

    public RGOEmploymentSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        ecsWorld.system("RGOEmploymentSystem")
            .kind(phaseId)
            .with(ResourceGathering.class)
            .with(Demographics.class)
            .with(ResourceGatheringType.class).up(ecsConstants.rgoType())
            .with(Province.class)
            .multiThreaded()
            .iter(this::hire);
    }

    private void hire(Iter iter) {
        if (iter.count() == 0) {
            return;
        }
        Field<ResourceGathering> resourceGatheringField = iter.field(ResourceGathering.class, 0);
        Field<Demographics> demographicsField = iter.field(Demographics.class, 1);
        ResourceGatheringTypeView rgoTypeData = iter.field(ResourceGatheringType.class, 2).getView(0);
        Field<Province> provinceField = iter.field(Province.class, 3);
        int workforce = rgoTypeData.workforce();
        float workerPopTypeRatio = rgoTypeData.workerPopTypeRatio();
        float slavePopTypeRatio = rgoTypeData.slavePopTypeRatio();
        int workerIndex = rgoTypeData.workerPopTypeIndex();
        int slaveIndex = rgoTypeData.slavePopTypeIndex();

        ResourceGatheringView firstResourceGathering = resourceGatheringField.getMutView(0);
        int goodIndex = firstResourceGathering.goodIndex();
        GlobalMarketView globalMarket = iter.world().obtainEntityView(iter.world().lookup("global_market")).getView(GlobalMarket.class);
        float worldDemand = globalMarket.goodDemandAmounts(goodIndex);
        float inventory = globalMarket.goodStockAmounts(goodIndex) - globalMarket.goodProductionAmounts(goodIndex);
        boolean stocksAboveTarget = inventory > STOCK_TARGET_DAYS * worldDemand;

        float workerBaseShare = EmploymentUtils.baseWorkforceShares(iter.world())[workerIndex];
        long countryId = 0;
        float workforceShare = 0f;
        for (int i = 0; i < iter.count(); i++) {
            ResourceGatheringMutView resourceGathering = resourceGatheringField.getMutView(i);
            DemographicsView demographics = demographicsField.getMutView(i);
            ProvinceView province = provinceField.getMutView(i);
            if (province.ownerId() != countryId) {
                countryId = province.ownerId();
                CountryPopulationPolicyView populationPolicy = iter.world().obtainEntityView(countryId).getView(CountryPopulationPolicy.class);
                workforceShare = activeShare(workerBaseShare, populationPolicy.workforceShareModifier());
            }
            int activeWorkerAmount = activeWorkers(demographics.totalByPopType(workerIndex), workforceShare);

            int maxCapacity = resourceGathering.size() * workforce;
            int targetWorkers = (int) (maxCapacity * workerPopTypeRatio);
            int targetSlaves = (int) (maxCapacity * slavePopTypeRatio);
            int maximumWorkers = Math.min(targetWorkers, activeWorkerAmount);

            float adjustment = RGO_EMPLOYMENT_ADJUSTMENT_RATE * Math.clamp(resourceGathering.unitMargin(), -1f, 1f);
            if (adjustment > 0f && stocksAboveTarget || adjustment < 0f && !stocksAboveTarget) {
                adjustment = 0f;
            }
            int workerAmount = resourceGathering.workerAmount() + Math.round(adjustment * maximumWorkers);
            resourceGathering.vacancies(adjustment > 0f ? Math.max(0, targetWorkers - activeWorkerAmount) : 0);
            resourceGathering.applicants(0f);
            resourceGathering.workerAmount(Math.max(0, Math.min(maximumWorkers, workerAmount)));
            resourceGathering.slaveAmount(Math.min(targetSlaves, demographics.totalByPopType(slaveIndex)));
        }
    }
}

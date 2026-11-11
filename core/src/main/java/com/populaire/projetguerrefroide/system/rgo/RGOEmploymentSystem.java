package com.populaire.projetguerrefroide.system.rgo;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;

import static com.populaire.projetguerrefroide.simulation.Constants.*;
import static com.populaire.projetguerrefroide.system.population.EmploymentUtils.activeWorkers;
import static com.populaire.projetguerrefroide.system.population.EmploymentUtils.activeShare;

public class RGOEmploymentSystem {
    private final EcsConstants ecsConstants;

    public RGOEmploymentSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
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
        GlobalMarketView globalMarket = iter.stage().obtainEntityView(this.ecsConstants.globalMarket()).getView(GlobalMarket.class);
        float worldDemand = globalMarket.goodDemandAmounts(goodIndex);
        float inventory = globalMarket.goodStockAmounts(goodIndex) - globalMarket.goodProductionAmounts(goodIndex);
        boolean stocksAboveTarget = inventory > STOCK_TARGET_DAYS * worldDemand;

        float workerBaseShare = this.ecsConstants.baseWorkforceShares()[workerIndex];
        long countryId = 0;
        float workforceShare = 0f;
        for (int i = 0; i < iter.count(); i++) {
            ResourceGatheringMutView resourceGathering = resourceGatheringField.getMutView(i);
            DemographicsView demographics = demographicsField.getMutView(i);
            ProvinceView province = provinceField.getMutView(i);
            if (province.ownerId() != countryId) {
                countryId = province.ownerId();
                CountryPopulationPolicyView populationPolicy = iter.stage().obtainEntityView(countryId).getView(CountryPopulationPolicy.class);
                workforceShare = activeShare(workerBaseShare, populationPolicy.workforceShareModifier());
            }
            int activeWorkerAmount = activeWorkers(demographics.totalByPopType(workerIndex), workforceShare);

            float capacity = resourceGathering.endowment() * workforce;
            int targetWorkers = (int) (capacity * workerPopTypeRatio);
            int targetSlaves = (int) (capacity * slavePopTypeRatio);
            if (resourceGathering.endowmentShare() > 0f) {
                // endowment RGOs work as family farms (annex 10, § 10.20 and 10.21): every active worker of the type works the
                // endowment and the income is what it yields; no hiring or firing on the margin, the number of
                // workers follows the population (births, promotions, migrations), with diminishing returns
                resourceGathering.employmentRemainder(0f);
                resourceGathering.vacancies(0);
                resourceGathering.applicants(0f);
                resourceGathering.workerAmount(activeWorkerAmount);
                resourceGathering.slaveAmount(Math.min(targetSlaves, demographics.totalByPopType(slaveIndex)));
                continue;
            }
            int maximumWorkers = Math.min(targetWorkers, activeWorkerAmount);

            float adjustment = RGO_EMPLOYMENT_ADJUSTMENT_RATE * Math.clamp(resourceGathering.unitMargin(), -1f, 1f);
            if (adjustment > 0f && stocksAboveTarget || adjustment < 0f && !stocksAboveTarget) {
                adjustment = 0f;
            }
            float remainder = resourceGathering.employmentRemainder();
            if (adjustment == 0f || adjustment > 0f != remainder > 0f) {
                remainder = 0f;
            }
            float step = adjustment * maximumWorkers + remainder;
            int moved = (int) step;
            int workerAmount = resourceGathering.workerAmount() + moved;
            int boundedWorkerAmount = Math.max(0, Math.min(maximumWorkers, workerAmount));
            resourceGathering.employmentRemainder(boundedWorkerAmount == workerAmount ? step - moved : 0f);
            resourceGathering.vacancies(adjustment > 0f ? Math.max(0, targetWorkers - activeWorkerAmount) : 0);
            resourceGathering.applicants(0f);
            resourceGathering.workerAmount(boundedWorkerAmount);
            resourceGathering.slaveAmount(Math.min(targetSlaves, demographics.totalByPopType(slaveIndex)));
        }
    }
}

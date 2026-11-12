package com.populaire.projetguerrefroide.system.rgo;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;
import com.populaire.projetguerrefroide.system.population.PopulationMobilityUtils;

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
        boolean month = PopulationMobilityUtils.isMobilityDay(iter.stage(), this.ecsConstants);
        long countryId = 0;
        float workforceShare = 0f;
        CountryMarketView countryMarket = null;
        for (int i = 0; i < iter.count(); i++) {
            ResourceGatheringMutView resourceGathering = resourceGatheringField.getMutView(i);
            DemographicsView demographics = demographicsField.getMutView(i);
            ProvinceView province = provinceField.getMutView(i);
            if (province.ownerId() != countryId) {
                countryId = province.ownerId();
                EntityView country = iter.stage().obtainEntityView(countryId);
                CountryPopulationPolicyView populationPolicy = country.getView(CountryPopulationPolicy.class);
                workforceShare = activeShare(workerBaseShare, populationPolicy.workforceShareModifier());
                countryMarket = country.getView(CountryMarket.class);
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
                int neededWorkers = RgoUtils.neededWorkers(activeWorkerAmount, RgoUtils.addedLevel(resourceGathering));
                if (month) {
                    this.updateFamilyShare(resourceGathering, countryMarket, workerIndex, neededWorkers);
                }
                resourceGathering.workerAmount(RgoUtils.familyWorkers(neededWorkers, resourceGathering.familyShare()));
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

    /**
     * Departure and return of the family farm's workers (annex 12, decision 197), monthly. R = what working the farm paid
     * per worker over the month (wages paid and the workers' share of the profit), S = the subsistence a worker gets off
     * the farm (75 % of his life needs at the country's prices). Below S, a share 1 % x (1 - R / S) of the workers leaves
     * the farm; above, a share 1 % x min(1, R / S - 1) of those who left comes back, never beyond the needed workers.
     * Without workers, R is the output of a first worker at the country's price (decision T15).
     */
    private void updateFamilyShare(ResourceGatheringMutView rgo, CountryMarketView countryMarket, int workerIndex, int neededWorkers) {
        float subsistence = SUBSISTENCE_LIFE_SHARE * countryMarket.lifeCostsByPopType(workerIndex) / NEEDS_SCALING_FACTOR;
        float income = rgo.monthWorkerDays() > 0f ? rgo.monthIncome() / rgo.monthWorkerDays()
            : rgo.workerOutput() * countryMarket.goodPrices(rgo.goodIndex());
        rgo.monthIncome(0f);
        rgo.monthWorkerDays(0f);
        if (subsistence <= 0f || neededWorkers <= 0) {
            rgo.familyRatio(0f);
            return;
        }
        float ratio = income / subsistence;
        rgo.familyRatio(ratio);
        int workers = RgoUtils.familyWorkers(neededWorkers, rgo.familyShare());
        if (ratio < 1f) {
            workers -= (int) Math.ceil(PROMOTION_RATE * (1f - ratio) * workers);
        } else if (ratio > 1f) {
            workers += (int) Math.ceil(PROMOTION_RATE * Math.min(1f, ratio - 1f) * (neededWorkers - workers));
        }
        rgo.familyShare(Math.clamp((float) workers / neededWorkers, 0f, 1f));
    }
}

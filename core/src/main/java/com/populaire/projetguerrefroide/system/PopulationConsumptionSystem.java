package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.*;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;
import com.populaire.projetguerrefroide.util.SubsistenceUtils;

import static com.populaire.projetguerrefroide.util.Constants.*;

public class PopulationConsumptionSystem {

    private final EcsConstants ecsConstants;

    public PopulationConsumptionSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("PopulationConsumptionSystem")
            .kind(phaseId)
            .with(Population.class)
            .iter(this::consume);
    }

    private void consume(Iter iter) {
        long countryId = 0;
        CountryMarketMutView countryMarket = null;
        CountryBudgetPolicyView countryBudgetPolicy = null;

        EntityView globalPopType = iter.world().obtainEntityView(iter.world().lookup("global_population_type"));
        GlobalPopulationTypeView globalPopTypeData = globalPopType.getMutView(GlobalPopulationType.class);
        float[] spendingRatesByPopType = new float[POP_TYPE_COUNT];
        boolean[] ruralByPopType = new boolean[POP_TYPE_COUNT];
        for (int ptIndex = 0; ptIndex < globalPopTypeData.popTypeIdsLength(); ptIndex++) {
            EntityView popType = iter.world().obtainEntityView(globalPopTypeData.popTypeIds(ptIndex));
            PopulationTypeView popTypeData = popType.getMutView(PopulationType.class);
            spendingRatesByPopType[ptIndex] = SPENDING_RATES_BY_STRATA[popTypeData.strata()];
            ruralByPopType[ptIndex] = popType.has(this.ecsConstants.ruralTag());
        }

        Field<Population> popField = iter.field(Population.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            PopulationMutView pop = popField.getMutView(i);

            if(countryId != pop.countryId()) {
                countryId = pop.countryId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
                countryBudgetPolicy = country.getMutView(CountryBudgetPolicy.class);
            }

            int popTypeIndex = pop.index();

            float spendingRate = spendingRatesByPopType[popTypeIndex] * (1f + countryBudgetPolicy.popSpending());
            float budget = spendingRate * pop.savings();
            float scaledAmount = pop.amount() / NEEDS_SCALING_FACTOR;

            float marketLifeShare = 1f - SubsistenceUtils.coveredLifeShare(ruralByPopType[popTypeIndex], pop.amount(), pop.employment());
            float neededLife = marketLifeShare * countryMarket.lifeBudgetCostsByPopType(popTypeIndex) * scaledAmount;
            float lifeSpending = Math.min(budget, neededLife);
            float lifeFraction = marketLifeShare * (neededLife > 0f ? lifeSpending / neededLife : 1f);
            budget -= lifeSpending;

            float neededEveryday = countryMarket.everydayBudgetCostsByPopType(popTypeIndex) * scaledAmount;
            float everydaySpending = Math.min(budget, neededEveryday);
            float everydayFraction = neededEveryday > 0f ? everydaySpending / neededEveryday : 1f;
            budget -= everydaySpending;

            float neededLuxury = countryMarket.luxuryBudgetCostsByPopType(popTypeIndex) * scaledAmount;
            float luxuryFraction = neededLuxury > 0f ? Math.min(budget, MAXIMUM_LUXURY_BASKETS * neededLuxury) / neededLuxury : 1f;

            pop.lifeNeedsDemandRatio(lifeFraction);
            pop.everydayNeedsDemandRatio(everydayFraction);
            pop.luxuryNeedsDemandRatio(luxuryFraction);

            countryMarket.lifeDemandUnitsByPopType(popTypeIndex, countryMarket.lifeDemandUnitsByPopType(popTypeIndex) + (double) scaledAmount * lifeFraction);
            countryMarket.everydayDemandUnitsByPopType(popTypeIndex, countryMarket.everydayDemandUnitsByPopType(popTypeIndex) + (double) scaledAmount * everydayFraction);
            countryMarket.luxuryDemandUnitsByPopType(popTypeIndex, countryMarket.luxuryDemandUnitsByPopType(popTypeIndex) + (double) scaledAmount * luxuryFraction);
        }
    }
}

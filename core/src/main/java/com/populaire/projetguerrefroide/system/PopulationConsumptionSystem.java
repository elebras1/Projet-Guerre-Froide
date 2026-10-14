package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.*;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.Constants.NEEDS_SCALING_FACTOR;

public class PopulationConsumptionSystem {

    public PopulationConsumptionSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("PopulationConsumptionSystem")
            .kind(phaseId)
            .with(Population.class)
            .iter(this::consume);
    }

    private void consume(Iter iter) {
        long countryId = 0;
        CountryMarketMutView countryMarket = null;
        CountryBudgetPolicyView countryBudgetPolicy = null;

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

            float budget = pop.savings() * (1.0f - countryBudgetPolicy.popSpending());
            float scaledAmount = pop.amount() / NEEDS_SCALING_FACTOR;

            float neededLife = countryMarket.lifeBudgetCostsByPopType(popTypeIndex) * scaledAmount;
            float lifeSpending = Math.min(budget, neededLife);
            float lifeFraction = neededLife > 0f ? lifeSpending / neededLife : 1f;
            budget -= lifeSpending;

            float neededEveryday = countryMarket.everydayBudgetCostsByPopType(popTypeIndex) * scaledAmount;
            float everydaySpending = Math.min(budget, neededEveryday);
            float everydayFraction = neededEveryday > 0f ? everydaySpending / neededEveryday : 1f;
            budget -= everydaySpending;

            float neededLuxury = countryMarket.luxuryBudgetCostsByPopType(popTypeIndex) * scaledAmount;
            float luxuryFraction = neededLuxury > 0f ? Math.min(budget, neededLuxury) / neededLuxury : 1f;

            pop.lifeNeedsSatisfaction(lifeFraction);
            pop.everydayNeedsSatisfaction(everydayFraction);
            pop.luxuryNeedsSatisfaction(luxuryFraction);

            EntityView popType = iter.world().obtainEntityView(pop.typeId());
            PopulationTypeView popTypeData = popType.getMutView(PopulationType.class);

            for (int j = 0; j < popTypeData.lifeNeedsGoodAmountsLength(); j++) {
                int goodIndex = popTypeData.lifeNeedsGoodIndexes(j);
                if (goodIndex < 0) {
                    break;
                }
                float base = popTypeData.lifeNeedsGoodAmounts(j);
                float demand = base * lifeFraction * scaledAmount;
                countryMarket.goodDemandAmounts(goodIndex, countryMarket.goodDemandAmounts(goodIndex) + demand);
                countryMarket.goodPopulationDemandAmounts(goodIndex, countryMarket.goodPopulationDemandAmounts(goodIndex) + demand);
            }

            for (int j = 0; j < popTypeData.everydayNeedsGoodIndexesLength(); j++) {
                int goodIndex = popTypeData.everydayNeedsGoodIndexes(j);
                if (goodIndex < 0) {
                    break;
                }
                float base = popTypeData.everydayNeedsGoodAmounts(j);
                float demand = base * everydayFraction * scaledAmount;
                countryMarket.goodDemandAmounts(goodIndex, countryMarket.goodDemandAmounts(goodIndex) + demand);
                countryMarket.goodPopulationDemandAmounts(goodIndex, countryMarket.goodPopulationDemandAmounts(goodIndex) + demand);
            }

            for (int j = 0; j < popTypeData.luxuryNeedsGoodIndexesLength(); j++) {
                int goodIndex = popTypeData.luxuryNeedsGoodIndexes(j);
                if (goodIndex < 0) {
                    break;
                }
                float base = popTypeData.luxuryNeedsGoodAmounts(j);
                float demand = base * luxuryFraction * scaledAmount;
                countryMarket.goodDemandAmounts(goodIndex, countryMarket.goodDemandAmounts(goodIndex) + demand);
                countryMarket.goodPopulationDemandAmounts(goodIndex, countryMarket.goodPopulationDemandAmounts(goodIndex) + demand);
            }
        }
    }
}

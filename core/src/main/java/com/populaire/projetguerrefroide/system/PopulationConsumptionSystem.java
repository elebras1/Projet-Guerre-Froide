package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.*;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;
import com.populaire.projetguerrefroide.util.SubsistenceUtils;

import static com.populaire.projetguerrefroide.util.Constants.*;

public class PopulationConsumptionSystem {

    public PopulationConsumptionSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        ecsWorld.system("PopulationConsumptionSystem")
            .kind(phaseId)
            .with(Population.class)
            .with(PopulationType.class)
            .with(ecsConstants.ruralTag()).optional()
            .iter(this::consume);
    }

    private void consume(Iter iter) {
        long countryId = 0;
        CountryMarketMutView countryMarket = null;
        CountryBudgetPolicyView countryBudgetPolicy = null;
        CountryBankMutView countryBank = null;

        Field<PopulationType> populationTypeField = iter.field(PopulationType.class, 1);
        PopulationTypeView populationTypeData = populationTypeField.getView(0);
        int popTypeIndex = populationTypeData.index();
        float strataSpendingRate = SPENDING_RATES_BY_STRATA[populationTypeData.strata()];
        boolean rural = iter.isSet(2);

        Field<Population> popField = iter.field(Population.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            PopulationMutView pop = popField.getMutView(i);

            if(countryId != pop.countryId()) {
                countryId = pop.countryId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
                countryBudgetPolicy = country.getMutView(CountryBudgetPolicy.class);
                countryBank = country.getMutView(CountryBank.class);
            }

            float spendingRate = strataSpendingRate * (1f + countryBudgetPolicy.popSpending());
            float budget = spendingRate * pop.savings();
            float scaledAmount = pop.amount() / NEEDS_SCALING_FACTOR;

            float marketLifeShare = 1f - SubsistenceUtils.coveredLifeShare(rural, pop.amount(), pop.employment());
            float neededLife = marketLifeShare * countryMarket.lifeBudgetCostsByPopType(popTypeIndex) * scaledAmount;
            float neededEveryday = countryMarket.everydayBudgetCostsByPopType(popTypeIndex) * scaledAmount;
            float neededLuxury = countryMarket.luxuryBudgetCostsByPopType(popTypeIndex) * scaledAmount;
            float shortfall = neededLife + neededEveryday + MAXIMUM_LUXURY_BASKETS * neededLuxury - budget;
            if (shortfall > 0f) {
                float withdrawal = (float) Math.min(shortfall, Math.min(pop.deposit(), Math.max(0.0, countryBank.reserves())));
                if (withdrawal > 0f) {
                    pop.deposit(pop.deposit() - withdrawal);
                    countryBank.reserves(countryBank.reserves() - withdrawal);
                    pop.savings(pop.savings() + withdrawal);
                    budget += withdrawal;
                }
            }

            float lifeSpending = Math.min(budget, neededLife);
            float lifeFraction = marketLifeShare * (neededLife > 0f ? lifeSpending / neededLife : 1f);
            budget -= lifeSpending;

            float everydaySpending = Math.min(budget, neededEveryday);
            float everydayFraction = neededEveryday > 0f ? everydaySpending / neededEveryday : 1f;
            budget -= everydaySpending;

            float luxurySpending = neededLuxury > 0f ? Math.min(budget, MAXIMUM_LUXURY_BASKETS * neededLuxury) : 0f;
            float luxuryFraction = neededLuxury > 0f ? luxurySpending / neededLuxury : 1f;
            pop.unspentBudget(budget - luxurySpending);

            pop.lifeNeedsDemandRatio(lifeFraction);
            pop.everydayNeedsDemandRatio(everydayFraction);
            pop.luxuryNeedsDemandRatio(luxuryFraction);

            countryMarket.lifeDemandUnitsByPopType(popTypeIndex, countryMarket.lifeDemandUnitsByPopType(popTypeIndex) + (double) scaledAmount * lifeFraction);
            countryMarket.everydayDemandUnitsByPopType(popTypeIndex, countryMarket.everydayDemandUnitsByPopType(popTypeIndex) + (double) scaledAmount * everydayFraction);
            countryMarket.luxuryDemandUnitsByPopType(popTypeIndex, countryMarket.luxuryDemandUnitsByPopType(popTypeIndex) + (double) scaledAmount * luxuryFraction);
        }
    }
}

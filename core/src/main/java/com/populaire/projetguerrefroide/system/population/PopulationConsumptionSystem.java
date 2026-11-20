package com.populaire.projetguerrefroide.system.population;

import io.github.elebras1.flecs.*;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;
import com.populaire.projetguerrefroide.system.state.BankUtils;

import static com.populaire.projetguerrefroide.simulation.Constants.*;

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
                EntityView country = iter.stage().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
                countryBudgetPolicy = country.getMutView(CountryBudgetPolicy.class);
                countryBank = country.getMutView(CountryBank.class);
            }

            float spendingRate = strataSpendingRate * (1f + countryBudgetPolicy.popSpending());
            float budget = spendingRate * pop.savings();
            float scaledAmount = pop.amount() / NEEDS_SCALING_FACTOR;

            float marketLifeShare = 1f - SubsistenceUtils.coveredLifeShare(rural, pop.amount(), pop.employment());
            float neededLife = marketLifeShare * countryMarket.lifeBudgetCostsByPopType(popTypeIndex) * scaledAmount;
            float everydayBasket = countryMarket.everydayBudgetCostsByPopType(popTypeIndex) * scaledAmount;
            float neededLuxury = countryMarket.luxuryBudgetCostsByPopType(popTypeIndex) * scaledAmount;
            float wealthSpending = HOUSEHOLD_WEALTH_SPENDING_RATE / DAYS_PER_YEAR * pop.deposit();
            float factor = needsFactor(pop.incomeTrend() * scaledAmount, wealthSpending, neededLife, everydayBasket + MAXIMUM_LUXURY_BASKETS * neededLuxury);
            float neededEveryday = everydayBasket * factor;
            float luxuryBaskets = MAXIMUM_LUXURY_BASKETS * factor;
            float shortfall = neededLife + neededEveryday + luxuryBaskets * neededLuxury - budget;
            if (shortfall > 0f) {
                float withdrawal = Math.min(shortfall, pop.deposit());
                if (withdrawal > 0f) {
                    pop.deposit(pop.deposit() - withdrawal);
                    BankUtils.draw(countryBank, withdrawal);
                    pop.savings(pop.savings() + withdrawal);
                    budget += withdrawal;
                }
            }

            float lifeSpending = Math.min(budget, neededLife);
            float lifeFraction = marketLifeShare * (neededLife > 0f ? lifeSpending / neededLife : 1f);
            budget -= lifeSpending;

            float everydaySpending = Math.min(budget, neededEveryday);
            float everydayFraction = everydayBasket > 0f ? everydaySpending / everydayBasket : 1f;
            budget -= everydaySpending;

            float luxurySpending = neededLuxury > 0f ? Math.min(budget, luxuryBaskets * neededLuxury) : 0f;
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

    static float needsFactor(float income, float wealthSpending, float neededLife, float growingNeeds) {
        if (growingNeeds <= 0f) {
            return 1f;
        }
        float incomeSpending = Math.max(neededLife + growingNeeds, (1f - HOUSEHOLD_SAVING_SHARE) * income);
        return (incomeSpending + wealthSpending - neededLife) / growingNeeds;
    }
}

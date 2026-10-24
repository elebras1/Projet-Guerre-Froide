package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.StateBudgetUtils;
import com.populaire.projetguerrefroide.util.StateConsumptionUtils;

public class StateConsumptionDemandSystem {

    public StateConsumptionDemandSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("StateConsumptionDemandSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .with(CountryDemographics.class)
            .with(CountryBudgetPolicy.class)
            .with(CountryBudget.class)
            .with(CountryStateConsumption.class)
            .iter(this::demand);
    }

    private void demand(Iter iter) {
        GlobalStateConsumptionView stateConsumption = StateConsumptionUtils.globalStateConsumption(iter.world());
        int statePostIndex = StateBudgetUtils.statePostIndex(iter.world());

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        Field<CountryDemographics> countryDemographicsField = iter.field(CountryDemographics.class, 1);
        Field<CountryBudgetPolicy> countryBudgetPolicyField = iter.field(CountryBudgetPolicy.class, 2);
        Field<CountryBudget> countryBudgetField = iter.field(CountryBudget.class, 3);
        Field<CountryStateConsumption> countryStateConsumptionField = iter.field(CountryStateConsumption.class, 4);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
            CountryDemographicsView countryDemographics = countryDemographicsField.getMutView(i);
            CountryBudgetPolicyView budgetPolicy = countryBudgetPolicyField.getMutView(i);
            CountryBudgetView countryBudget = countryBudgetField.getMutView(i);
            CountryStateConsumptionMutView countryStateConsumption = countryStateConsumptionField.getMutView(i);

            for (int g = 0; g < countryStateConsumption.demandedAmountsLength(); g++) {
                countryStateConsumption.demandedAmounts(g, 0f);
            }
            float spendingRatio = countryBudget.spendingRatio();
            float administrationUnits = StateConsumptionUtils.administrationUnits(countryDemographics, statePostIndex, budgetPolicy) * spendingRatio;
            float publicHealthUnits = StateConsumptionUtils.publicHealthUnits(countryDemographics, budgetPolicy) * spendingRatio;
            for (int k = 0; k < stateConsumption.administrationGoodIndexesLength(); k++) {
                int goodIndex = stateConsumption.administrationGoodIndexes(k);
                if (goodIndex < 0) {
                    break;
                }
                this.add(countryMarket, countryStateConsumption, goodIndex, stateConsumption.administrationGoodAmounts(k) * administrationUnits);
            }
            for (int k = 0; k < stateConsumption.publicHealthGoodIndexesLength(); k++) {
                int goodIndex = stateConsumption.publicHealthGoodIndexes(k);
                if (goodIndex < 0) {
                    break;
                }
                this.add(countryMarket, countryStateConsumption, goodIndex, stateConsumption.publicHealthGoodAmounts(k) * publicHealthUnits);
            }
        }
    }

    private void add(CountryMarketMutView countryMarket, CountryStateConsumptionMutView countryStateConsumption, int goodIndex, float amount) {
        if (amount <= 0f) {
            return;
        }
        countryStateConsumption.demandedAmounts(goodIndex, countryStateConsumption.demandedAmounts(goodIndex) + amount);
        countryMarket.goodDemandAmounts(goodIndex, countryMarket.goodDemandAmounts(goodIndex) + amount);
    }
}

package com.populaire.projetguerrefroide.system.state;

import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;

import static com.populaire.projetguerrefroide.simulation.Constants.NEEDS_SCALING_FACTOR;

public class StateConsumptionUtils {

    public static GlobalStateConsumptionView globalStateConsumption(World world, EcsConstants ecsConstants) {
        return world.obtainEntityView(ecsConstants.globalStateConsumption()).getView(GlobalStateConsumption.class);
    }

    public static float administrationUnits(CountryDemographicsView countryDemographics, int statePostIndex, CountryBudgetPolicyView budgetPolicy) {
        if (statePostIndex < 0) {
            return 0f;
        }
        return countryDemographics.employmentByPopType(statePostIndex) / NEEDS_SCALING_FACTOR * StateBudgetUtils.slider(budgetPolicy.administrationSpendingRate());
    }

    public static float publicHealthUnits(CountryDemographicsView countryDemographics, CountryBudgetPolicyView budgetPolicy) {
        return countryDemographics.totalAdults() / NEEDS_SCALING_FACTOR * budgetPolicy.publicHealthRate();
    }

    public static double dailyCost(GlobalStateConsumptionView stateConsumption, CountryMarketView countryMarket, float administrationUnits, float publicHealthUnits) {
        double cost = 0.0;
        for (int k = 0; k < stateConsumption.administrationGoodIndexesLength(); k++) {
            int goodIndex = stateConsumption.administrationGoodIndexes(k);
            if (goodIndex < 0) {
                break;
            }
            cost += (double) stateConsumption.administrationGoodAmounts(k) * administrationUnits * countryMarket.goodBudgetPrices(goodIndex);
        }
        for (int k = 0; k < stateConsumption.publicHealthGoodIndexesLength(); k++) {
            int goodIndex = stateConsumption.publicHealthGoodIndexes(k);
            if (goodIndex < 0) {
                break;
            }
            cost += (double) stateConsumption.publicHealthGoodAmounts(k) * publicHealthUnits * countryMarket.goodBudgetPrices(goodIndex);
        }
        return cost;
    }
}

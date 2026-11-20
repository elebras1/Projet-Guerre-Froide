package com.populaire.projetguerrefroide.system.population;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;

import static com.populaire.projetguerrefroide.simulation.Constants.*;

public class PopulationSavingsInitializationSystem {

    public PopulationSavingsInitializationSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        ecsWorld.system("PopulationSavingsInitializationSystem")
            .kind(phaseId)
            .with(Population.class)
            .with(PopulationType.class)
            .with(ecsConstants.capitalistTag()).optional()
            .iter(this::initialize);
    }

    private void initialize(Iter iter) {
        PopulationTypeView populationTypeData = iter.field(PopulationType.class, 1).getView(0);
        boolean capitalist = iter.isSet(2);
        float strataSpendingRate = SPENDING_RATES_BY_STRATA[populationTypeData.strata()];

        long countryId = 0;
        float spendingRate = strataSpendingRate;
        CountryBankMutView countryBank = null;
        Field<Population> populationField = iter.field(Population.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            PopulationMutView population = populationField.getMutView(i);
            if (population.countryId() != countryId) {
                countryId = population.countryId();
                EntityView country = iter.stage().obtainEntityView(countryId);
                CountryBudgetPolicyView countryBudgetPolicy = country.getView(CountryBudgetPolicy.class);
                spendingRate = strataSpendingRate * (1f + countryBudgetPolicy.popSpending());
                countryBank = country.getMutView(CountryBank.class);
            }
            population.savings(Math.max(0f, population.income()) / spendingRate);
            if (population.unspentBudget() > 0f && !capitalist) {
                float deposit = population.unspentBudget() * DAYS_PER_YEAR / HOUSEHOLD_WEALTH_SPENDING_RATE;
                countryBank.reserves(countryBank.reserves() + deposit - population.deposit());
                population.deposit(deposit);
            }
        }
    }
}

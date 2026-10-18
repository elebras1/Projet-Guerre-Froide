package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.Constants.*;

public class PopulationSavingsInitializationSystem {

    public PopulationSavingsInitializationSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("PopulationSavingsInitializationSystem")
            .kind(phaseId)
            .with(Population.class)
            .with(PopulationType.class)
            .multiThreaded()
            .iter(this::initialize);
    }

    private void initialize(Iter iter) {
        PopulationTypeView populationTypeData = iter.field(PopulationType.class, 1).getView(0);
        float strataSpendingRate = SPENDING_RATES_BY_STRATA[populationTypeData.strata()];

        long countryId = 0;
        float spendingRate = strataSpendingRate;
        Field<Population> populationField = iter.field(Population.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            PopulationMutView population = populationField.getMutView(i);
            if (population.countryId() != countryId) {
                countryId = population.countryId();
                EntityView country = iter.world().obtainEntityView(countryId);
                CountryBudgetPolicyView countryBudgetPolicy = country.getView(CountryBudgetPolicy.class);
                spendingRate = strataSpendingRate * (1f + countryBudgetPolicy.popSpending());
            }
            population.savings(Math.max(0f, population.income()) / spendingRate);
        }
    }
}

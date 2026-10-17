package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.Constants.NEEDS_SCALING_FACTOR;
import static com.populaire.projetguerrefroide.util.IncomeTypeUtils.*;

public class CountrySpendingSystem {

    public CountrySpendingSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("CountrySpendingSystem")
            .kind(phaseId)
            .with(Population.class)
            .with(PopulationType.class)
            .iter(this::spend);
    }

    private void spend(Iter iter) {
        Field<PopulationType> populationTypeField = iter.field(PopulationType.class, 1);
        PopulationTypeView populationTypeData = populationTypeField.getView(0);
        int popTypeIndex = populationTypeData.index();
        int incomeType = populationTypeData.incomeType();

        long countryId = 0;
        CountryMarketMutView countryMarket = null;
        float budget = 0f;
        float totalWeight = 0f;

        Field<Population> populationField = iter.field(Population.class, 0);
        for(int i = 0; i < iter.count(); i++) {
            PopulationMutView population = populationField.getMutView(i);

            if(population.countryId() != countryId) {
                countryId = population.countryId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
                CountrySpendingBudgetView countrySpendingBudget = country.getMutView(CountrySpendingBudget.class);
                switch (incomeType) {
                    case ADMINISTRATION_INCOME_TYPE -> {
                        budget = countrySpendingBudget.administrationBudget();
                        totalWeight = countrySpendingBudget.administrationWeight();
                    }
                    case MILITARY_INCOME_TYPE -> {
                        budget = countrySpendingBudget.militaryBudget();
                        totalWeight = countrySpendingBudget.militaryWeight();
                    }
                    case EDUCATION_INCOME_TYPE -> {
                        budget = countrySpendingBudget.educationBudget();
                        totalWeight = countrySpendingBudget.educationWeight();
                    }
                    default -> {
                        budget = countrySpendingBudget.socialBudget();
                        totalWeight = countrySpendingBudget.socialWeight();
                    }
                }
            }

            float cost = countryMarket.lifeCostsByPopType(popTypeIndex) + countryMarket.everydayCostsByPopType(popTypeIndex) + countryMarket.luxuryCostsByPopType(popTypeIndex);
            float weight = population.amount() * cost / NEEDS_SCALING_FACTOR;
            float share = totalWeight > 0f ? budget * weight / totalWeight : 0f;

            population.savings(population.savings() + share);
            countryMarket.treasury(countryMarket.treasury() - share);
        }
    }
}

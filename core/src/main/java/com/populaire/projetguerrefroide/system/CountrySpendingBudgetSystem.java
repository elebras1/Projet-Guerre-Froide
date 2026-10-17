package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.Constants.NEEDS_SCALING_FACTOR;
import static com.populaire.projetguerrefroide.util.IncomeTypeUtils.*;

public class CountrySpendingBudgetSystem {

    public CountrySpendingBudgetSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("CountrySpendingBudgetSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .with(CountryDemographics.class)
            .with(CountryBudgetPolicy.class)
            .with(CountryEducationPolicy.class)
            .with(CountrySpendingBudget.class)
            .multiThreaded()
            .iter(this::budget);
    }

    private void budget(Iter iter) {
        EntityView globalPopType = iter.world().obtainEntityView(iter.world().lookup("global_population_type"));
        GlobalPopulationTypeView globalPopTypeData = globalPopType.getMutView(GlobalPopulationType.class);

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        Field<CountryDemographics> countryDemographicsField = iter.field(CountryDemographics.class, 1);
        Field<CountryBudgetPolicy> countryBudgetPolicyField = iter.field(CountryBudgetPolicy.class, 2);
        Field<CountryEducationPolicy> countryEducationPolicyField = iter.field(CountryEducationPolicy.class, 3);
        Field<CountrySpendingBudget> countrySpendingBudgetField = iter.field(CountrySpendingBudget.class, 4);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketView countryMarket = countryMarketField.getMutView(i);
            CountryDemographicsView countryDemographics = countryDemographicsField.getMutView(i);
            CountryBudgetPolicyView countryBudgetPolicy = countryBudgetPolicyField.getMutView(i);
            CountryEducationPolicyView countryEducationPolicy = countryEducationPolicyField.getMutView(i);
            CountrySpendingBudgetMutView countrySpendingBudget = countrySpendingBudgetField.getMutView(i);

            float administrationWeight = 0f;
            float militaryWeight = 0f;
            float educationWeight = 0f;
            float socialWeight = 0f;
            for (int p = 0; p < globalPopTypeData.popTypeIdsLength(); p++) {
                EntityView popType = iter.world().obtainEntityView(globalPopTypeData.popTypeIds(p));
                PopulationTypeView popTypeData = popType.getMutView(PopulationType.class);
                float cost = countryMarket.lifeCostsByPopType(p) + countryMarket.everydayCostsByPopType(p) + countryMarket.luxuryCostsByPopType(p);
                float weight = countryDemographics.totalByPopType(p) * cost / NEEDS_SCALING_FACTOR;
                switch (popTypeData.incomeType()) {
                    case ADMINISTRATION_INCOME_TYPE -> administrationWeight += weight;
                    case MILITARY_INCOME_TYPE -> militaryWeight += weight;
                    case EDUCATION_INCOME_TYPE -> educationWeight += weight;
                    default -> socialWeight += weight;
                }
            }

            float administrationRate = countryBudgetPolicy.administrationSpendingRate();
            float militaryRate = countryBudgetPolicy.militarySpendingRate();
            float educationRate = countryEducationPolicy.educationSpendingRate();
            float socialRate = countryBudgetPolicy.socialSpendingRate();

            float activeRate = 0f;
            if (administrationWeight > 0f && administrationRate > 0f) {
                activeRate += administrationRate;
            }
            if (militaryWeight > 0f && militaryRate > 0f) {
                activeRate += militaryRate;
            }
            if (educationWeight > 0f && educationRate > 0f) {
                activeRate += educationRate;
            }
            if (socialWeight > 0f && socialRate > 0f) {
                activeRate += socialRate;
            }

            float treasury = (float) countryMarket.treasury();
            countrySpendingBudget.administrationBudget(administrationWeight > 0f && administrationRate > 0f && activeRate > 0f ? treasury * administrationRate / activeRate : 0f);
            countrySpendingBudget.militaryBudget(militaryWeight > 0f && militaryRate > 0f && activeRate > 0f ? treasury * militaryRate / activeRate : 0f);
            countrySpendingBudget.educationBudget(educationWeight > 0f && educationRate > 0f && activeRate > 0f ? treasury * educationRate / activeRate : 0f);
            countrySpendingBudget.socialBudget(socialWeight > 0f && socialRate > 0f && activeRate > 0f ? treasury * socialRate / activeRate : 0f);
            countrySpendingBudget.administrationWeight(administrationWeight);
            countrySpendingBudget.militaryWeight(militaryWeight);
            countrySpendingBudget.educationWeight(educationWeight);
            countrySpendingBudget.socialWeight(socialWeight);
        }
    }
}

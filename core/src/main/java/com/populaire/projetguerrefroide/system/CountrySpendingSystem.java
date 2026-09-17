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
            .iter(this::spend);
    }

    private void spend(Iter iter) {
        EntityView globalPopType = iter.world().obtainEntityView(iter.world().lookup("global_population_type"));
        GlobalPopulationTypeView globalPopTypeData = globalPopType.getMutView(GlobalPopulationType.class);

        long countryId = 0;
        CountryMarketView countryMarket = null;
        CountryDemographicsView countryDemographics;
        CountryBudgetPolicyView countryBudgetPolicy;
        CountryEducationPolicyView countryEducationPolicy;

        float administrationBudget = 0f;
        float militaryBudget = 0f;
        float educationBudget = 0f;
        float socialBudget = 0f;
        float administrationWeight = 0f;
        float militaryWeight = 0f;
        float educationWeight = 0f;
        float socialWeight = 0f;

        long populationTypeId = 0;
        PopulationTypeView populationTypeData = null;

        Field<Population> populationField = iter.field(Population.class, 0);
        for(int i = 0; i < iter.count(); i++) {
            PopulationView population = populationField.getMutView(i);

            if(population.countryId() != countryId) {
                countryId = population.countryId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
                countryDemographics = country.getMutView(CountryDemographics.class);
                countryBudgetPolicy = country.getMutView(CountryBudgetPolicy.class);
                countryEducationPolicy = country.getMutView(CountryEducationPolicy.class);

                administrationWeight = 0f;
                militaryWeight = 0f;
                educationWeight = 0f;
                socialWeight = 0f;
                for(int p = 0; p < globalPopTypeData.popTypeIdsLength(); p++) {
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
                if(administrationWeight > 0f && administrationRate > 0f) {
                    activeRate += administrationRate;
                }
                if(militaryWeight > 0f && militaryRate > 0f) {
                    activeRate += militaryRate;
                }
                if(educationWeight > 0f && educationRate > 0f) {
                    activeRate += educationRate;
                }
                if(socialWeight > 0f && socialRate > 0f) {
                    activeRate += socialRate;
                }

                float treasury = countryMarket.treasury();
                administrationBudget = administrationWeight > 0f && administrationRate > 0f && activeRate > 0f ? treasury * administrationRate / activeRate : 0f;
                militaryBudget = militaryWeight > 0f && militaryRate > 0f && activeRate > 0f ? treasury * militaryRate / activeRate : 0f;
                educationBudget = educationWeight > 0f && educationRate > 0f && activeRate > 0f ? treasury * educationRate / activeRate : 0f;
                socialBudget = socialWeight > 0f && socialRate > 0f && activeRate > 0f ? treasury * socialRate / activeRate : 0f;
            }

            if(population.typeId() != populationTypeId) {
                populationTypeId = population.typeId();
                EntityView populationType = iter.world().obtainEntityView(populationTypeId);
                populationTypeData = populationType.getMutView(PopulationType.class);
            }

            int popTypeIndex = population.index();
            float cost = countryMarket.lifeCostsByPopType(popTypeIndex) + countryMarket.everydayCostsByPopType(popTypeIndex) + countryMarket.luxuryCostsByPopType(popTypeIndex);
            float weight = population.amount() * cost / NEEDS_SCALING_FACTOR;

            float share;
            switch (populationTypeData.incomeType()) {
                case ADMINISTRATION_INCOME_TYPE -> share = administrationWeight > 0f ? administrationBudget * weight / administrationWeight : 0f;
                case MILITARY_INCOME_TYPE -> share = militaryWeight > 0f ? militaryBudget * weight / militaryWeight : 0f;
                case EDUCATION_INCOME_TYPE -> share = educationWeight > 0f ? educationBudget * weight / educationWeight : 0f;
                default -> share = socialWeight > 0f ? socialBudget * weight / socialWeight : 0f;
            }

            population.savings(population.savings() + share);
            countryMarket.treasury(countryMarket.treasury() - share);
        }
    }
}

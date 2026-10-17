package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.Constants.POP_TYPE_COUNT;
import static com.populaire.projetguerrefroide.util.Constants.TARGET_TREASURY_DAYS;
import static com.populaire.projetguerrefroide.util.StateBudgetUtils.dailyNeeds;
import static com.populaire.projetguerrefroide.util.StateBudgetUtils.readPopulationTypes;

public class CountryTreasuryInitializationSystem {

    public CountryTreasuryInitializationSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("CountryTreasuryInitializationSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .with(CountryDemographics.class)
            .with(CountryBudgetPolicy.class)
            .with(CountryEducationPolicy.class)
            .multiThreaded()
            .iter(this::initialize);
    }

    private void initialize(Iter iter) {
        int[] incomeTypes = new int[POP_TYPE_COUNT];
        boolean[] assistedTypes = new boolean[POP_TYPE_COUNT];
        readPopulationTypes(iter.world(), incomeTypes, assistedTypes);

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        Field<CountryDemographics> countryDemographicsField = iter.field(CountryDemographics.class, 1);
        Field<CountryBudgetPolicy> countryBudgetPolicyField = iter.field(CountryBudgetPolicy.class, 2);
        Field<CountryEducationPolicy> countryEducationPolicyField = iter.field(CountryEducationPolicy.class, 3);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
            CountryDemographicsView countryDemographics = countryDemographicsField.getMutView(i);
            CountryBudgetPolicyView countryBudgetPolicy = countryBudgetPolicyField.getMutView(i);
            CountryEducationPolicyView countryEducationPolicy = countryEducationPolicyField.getMutView(i);

            double needs = dailyNeeds(incomeTypes, assistedTypes, countryMarket, countryDemographics, countryBudgetPolicy, countryEducationPolicy);
            countryMarket.treasury(TARGET_TREASURY_DAYS * needs);
        }
    }
}

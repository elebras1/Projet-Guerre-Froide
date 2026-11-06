package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.StateConsumptionUtils;
import com.populaire.projetguerrefroide.util.EcsConstants;

import static com.populaire.projetguerrefroide.util.Constants.TARGET_TREASURY_DAYS;
import static com.populaire.projetguerrefroide.util.StateBudgetUtils.dailyNeeds;
import static com.populaire.projetguerrefroide.util.StateBudgetUtils.maintenanceCost;
import static com.populaire.projetguerrefroide.util.StateBudgetUtils.militarySupplyCost;

public class CountryTreasuryInitializationSystem {
    private final EcsConstants ecsConstants;

    public CountryTreasuryInitializationSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("CountryTreasuryInitializationSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .with(CountryDemographics.class)
            .with(CountryBudgetPolicy.class)
            .with(CountryEducationPolicy.class)
            .with(CountryPopulationPolicy.class)
            .with(CountryMilitary.class)
            .with(CountryMaintenance.class)
            .multiThreaded()
            .iter(this::initialize);
    }

    private void initialize(Iter iter) {
        int[] incomeTypes = this.ecsConstants.incomeTypes();
        boolean[] assistedTypes = this.ecsConstants.assistedTypes();
        int statePostIndex = this.ecsConstants.statePostIndex();
        float[] baseWorkforceShares = this.ecsConstants.baseWorkforceShares();
        GlobalStateConsumptionView stateConsumption = StateConsumptionUtils.globalStateConsumption(iter.world(), this.ecsConstants);

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        Field<CountryDemographics> countryDemographicsField = iter.field(CountryDemographics.class, 1);
        Field<CountryBudgetPolicy> countryBudgetPolicyField = iter.field(CountryBudgetPolicy.class, 2);
        Field<CountryEducationPolicy> countryEducationPolicyField = iter.field(CountryEducationPolicy.class, 3);
        Field<CountryPopulationPolicy> countryPopulationPolicyField = iter.field(CountryPopulationPolicy.class, 4);
        Field<CountryMilitary> countryMilitaryField = iter.field(CountryMilitary.class, 5);
        Field<CountryMaintenance> countryMaintenanceField = iter.field(CountryMaintenance.class, 6);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
            CountryDemographicsView countryDemographics = countryDemographicsField.getMutView(i);
            CountryBudgetPolicyView countryBudgetPolicy = countryBudgetPolicyField.getMutView(i);
            CountryEducationPolicyView countryEducationPolicy = countryEducationPolicyField.getMutView(i);

            CountryPopulationPolicyView countryPopulationPolicy = countryPopulationPolicyField.getMutView(i);
            CountryMilitaryView countryMilitary = countryMilitaryField.getMutView(i);
            CountryMaintenanceView countryMaintenance = countryMaintenanceField.getMutView(i);
            double needs = dailyNeeds(incomeTypes, assistedTypes, countryMarket, countryDemographics, countryBudgetPolicy, countryEducationPolicy, statePostIndex,
                baseWorkforceShares, countryPopulationPolicy.workforceShareModifier())
                + StateConsumptionUtils.dailyCost(stateConsumption, countryMarket,
                    StateConsumptionUtils.administrationUnits(countryDemographics, statePostIndex, countryBudgetPolicy),
                    StateConsumptionUtils.publicHealthUnits(countryDemographics, countryBudgetPolicy))
                + militarySupplyCost(countryMilitary, countryMarket)
                + maintenanceCost(countryMaintenance, countryMarket);
            countryMarket.treasury(TARGET_TREASURY_DAYS * needs);
        }
    }
}

package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.Constants.*;

public class LawEffectInitializationSystem {

    public LawEffectInitializationSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("LawEffectInitializationSystem")
            .kind(phaseId)
            .with(Country.class)
            .with(CountryTaxPolicy.class)
            .with(CountryBudgetPolicy.class)
            .with(CountryEducationPolicy.class)
            .with(CountryTradePolicy.class)
            .with(CountryProductionPolicy.class)
            .with(CountryLaborPolicy.class)
            .with(CountryPopulationPolicy.class)
            .with(CountryPoliticalPolicy.class)
            .with(CountryCulturePolicy.class)
            .with(CountryProfitDistributionPolicy.class)
            .multiThreaded()
            .iter(this::initialize);
    }

    private void initialize(Iter iter) {
        Field<Country> countryField = iter.field(Country.class, 0);
        Field<CountryTaxPolicy> taxPolicyField = iter.field(CountryTaxPolicy.class, 1);
        Field<CountryBudgetPolicy> budgetPolicyField = iter.field(CountryBudgetPolicy.class, 2);
        Field<CountryEducationPolicy> educationPolicyField = iter.field(CountryEducationPolicy.class, 3);
        Field<CountryTradePolicy> tradePolicyField = iter.field(CountryTradePolicy.class, 4);
        Field<CountryProductionPolicy> productionPolicyField = iter.field(CountryProductionPolicy.class, 5);
        Field<CountryLaborPolicy> laborPolicyField = iter.field(CountryLaborPolicy.class, 6);
        Field<CountryPopulationPolicy> populationPolicyField = iter.field(CountryPopulationPolicy.class, 7);
        Field<CountryPoliticalPolicy> politicalPolicyField = iter.field(CountryPoliticalPolicy.class, 8);
        Field<CountryCulturePolicy> culturePolicyField = iter.field(CountryCulturePolicy.class, 9);
        Field<CountryProfitDistributionPolicy> profitDistributionPolicyField = iter.field(CountryProfitDistributionPolicy.class, 10);

        for (int i = 0; i < iter.count(); i++) {
            CountryView country = countryField.getMutView(i);
            CountryTaxPolicyView taxPolicy = taxPolicyField.getMutView(i);
            CountryBudgetPolicyView budgetPolicy = budgetPolicyField.getMutView(i);
            CountryEducationPolicyView educationPolicy = educationPolicyField.getMutView(i);
            CountryTradePolicyView tradePolicy = tradePolicyField.getMutView(i);
            CountryProductionPolicyView productionPolicy = productionPolicyField.getMutView(i);
            CountryLaborPolicyView laborPolicy = laborPolicyField.getMutView(i);
            CountryPopulationPolicyView populationPolicy = populationPolicyField.getMutView(i);
            CountryPoliticalPolicyView politicalPolicy = politicalPolicyField.getMutView(i);
            CountryCulturePolicyView culturePolicy = culturePolicyField.getMutView(i);
            CountryProfitDistributionPolicyView profitDistributionPolicy = profitDistributionPolicyField.getMutView(i);

            for (int l = 0; l < country.activeLawIdsLength(); l++) {
                long lawId = country.activeLawIds(l);
                if (lawId == 0) {
                    continue;
                }

                EntityView law = iter.world().obtainEntityView(lawId);

                ModifiersView lawModifiers = law.getMutView(Modifiers.class);
                if (lawModifiers != null) {
                    for (int m = 0; m < lawModifiers.tagIdsLength(); m++) {
                        long modifierId = lawModifiers.tagIds(m);
                        if (modifierId <= 0) {
                            break;
                        }

                        float modifierValue = lawModifiers.values(m);
                        EntityView modifier = iter.world().obtainEntityView(modifierId);
                        String modifierName = modifier.name();

                        switch (modifierName) {
                            case TAX_POOR -> taxPolicy.poorTaxRate(taxPolicy.poorTaxRate() + modifierValue);
                            case TAX_MIDDLE -> taxPolicy.middleTaxRate(taxPolicy.middleTaxRate() + modifierValue);
                            case TAX_RICH -> taxPolicy.richTaxRate(taxPolicy.richTaxRate() + modifierValue);
                            case TARIFF_RATE -> tradePolicy.tariffRate(tradePolicy.tariffRate() + modifierValue);
                            case MILITARY_SPENDING -> budgetPolicy.militarySpendingRate(budgetPolicy.militarySpendingRate() + modifierValue);
                            case EDUCATION_SPENDING -> educationPolicy.educationSpendingRate(educationPolicy.educationSpendingRate() + modifierValue);
                            case ADMINISTRATION_SPENDING -> budgetPolicy.administrationSpendingRate(budgetPolicy.administrationSpendingRate() + modifierValue);
                            case SOCIAL_SPENDING -> budgetPolicy.socialSpendingRate(budgetPolicy.socialSpendingRate() + modifierValue);
                            case CAPITALIST_PROFIT_SHARE -> profitDistributionPolicy.capitalistProfitShareRate(profitDistributionPolicy.capitalistProfitShareRate() + modifierValue);
                            case WORKER_PROFIT_SHARE -> profitDistributionPolicy.workerProfitShareRate(profitDistributionPolicy.workerProfitShareRate() + modifierValue);
                            case ARISTOCRAT_PROFIT_SHARE -> profitDistributionPolicy.aristocratProfitShareRate(profitDistributionPolicy.aristocratProfitShareRate() + modifierValue);
                            case STATE_PROFIT_SHARE -> profitDistributionPolicy.stateProfitShareRate(profitDistributionPolicy.stateProfitShareRate() + modifierValue);
                            case MIN_WAGE_FACTOR -> laborPolicy.minWageFactor(laborPolicy.minWageFactor() + modifierValue);
                            case FACTORY_INPUT_MODIFIER -> productionPolicy.factoryInputModifier(productionPolicy.factoryInputModifier() + modifierValue);
                            case FACTORY_OUTPUT_MODIFIER -> productionPolicy.factoryOutputModifier(productionPolicy.factoryOutputModifier() + modifierValue);
                            case RGO_OUTPUT_MODIFIER -> productionPolicy.rgoOutputModifier(productionPolicy.rgoOutputModifier() + modifierValue);
                            case CONSTRUCTION_SPEED -> productionPolicy.constructionSpeed(productionPolicy.constructionSpeed() + modifierValue);
                            case POP_GROWTH_FACTOR -> populationPolicy.popGrowthFactor(populationPolicy.popGrowthFactor() + modifierValue);
                            case EDUCATION_EFFICIENCY -> educationPolicy.educationEfficiency(educationPolicy.educationEfficiency() + modifierValue);
                            case MIGRATION_PULL -> populationPolicy.migrationPull(populationPolicy.migrationPull() + modifierValue);
                            case POP_SPENDING -> budgetPolicy.popSpending(budgetPolicy.popSpending() + modifierValue);
                            case POLITICAL_CONSCIOUSNESS -> politicalPolicy.politicalConsciousness(politicalPolicy.politicalConsciousness() + modifierValue);
                            case POLITICAL_RADICALISM -> politicalPolicy.politicalRadicalism(politicalPolicy.politicalRadicalism() + modifierValue);
                            case SUPPRESSION -> politicalPolicy.suppression(politicalPolicy.suppression() + modifierValue);
                            case SOCIAL_MOBILITY -> politicalPolicy.socialMobility(politicalPolicy.socialMobility() + modifierValue);
                            case CLASS_RIGIDITY -> politicalPolicy.classRigidity(politicalPolicy.classRigidity() + modifierValue);
                            case ADMINISTRATIVE_EFFICIENCY -> politicalPolicy.administrativeEfficiency(politicalPolicy.administrativeEfficiency() + modifierValue);
                            case RELIGIOUS_CONVERSION_SPEED -> culturePolicy.religiousConversionSpeed(culturePolicy.religiousConversionSpeed() + modifierValue);
                            case SECULARISM -> culturePolicy.secularism(culturePolicy.secularism() + modifierValue);
                            case ASSIMILATION_RATE -> culturePolicy.assimilationRate(culturePolicy.assimilationRate() + modifierValue);
                            case MIGRATION_PUSH -> populationPolicy.migrationPush(populationPolicy.migrationPush() + modifierValue);
                        }
                    }
                }

                OverridesView lawOverrides = law.getMutView(Overrides.class);
                if (lawOverrides != null) {
                    for (int m = 0; m < lawOverrides.tagIdsLength(); m++) {
                        long overrideId = lawOverrides.tagIds(m);
                        if (overrideId <= 0) {
                            break;
                        }

                        float overrideValue = lawOverrides.values(m);
                        EntityView override = iter.world().obtainEntityView(overrideId);
                        String overrideName = override.name();

                        switch (overrideName) {
                            case TAX_POOR -> taxPolicy.poorTaxRate(overrideValue);
                            case TAX_MIDDLE -> taxPolicy.middleTaxRate(overrideValue);
                            case TAX_RICH -> taxPolicy.richTaxRate(overrideValue);
                            case TARIFF_RATE -> tradePolicy.tariffRate(overrideValue);
                            case MILITARY_SPENDING -> budgetPolicy.militarySpendingRate(overrideValue);
                            case EDUCATION_SPENDING -> educationPolicy.educationSpendingRate(overrideValue);
                            case ADMINISTRATION_SPENDING -> budgetPolicy.administrationSpendingRate(overrideValue);
                            case SOCIAL_SPENDING -> budgetPolicy.socialSpendingRate(overrideValue);
                            case CAPITALIST_PROFIT_SHARE -> profitDistributionPolicy.capitalistProfitShareRate(overrideValue);
                            case WORKER_PROFIT_SHARE -> profitDistributionPolicy.workerProfitShareRate(overrideValue);
                            case ARISTOCRAT_PROFIT_SHARE -> profitDistributionPolicy.aristocratProfitShareRate(overrideValue);
                            case STATE_PROFIT_SHARE -> profitDistributionPolicy.stateProfitShareRate(overrideValue);
                            case MIN_WAGE_FACTOR -> laborPolicy.minWageFactor(overrideValue);
                            case FACTORY_INPUT_MODIFIER -> productionPolicy.factoryInputModifier(overrideValue);
                            case FACTORY_OUTPUT_MODIFIER -> productionPolicy.factoryOutputModifier(overrideValue);
                            case RGO_OUTPUT_MODIFIER -> productionPolicy.rgoOutputModifier(overrideValue);
                            case CONSTRUCTION_SPEED -> productionPolicy.constructionSpeed(overrideValue);
                            case POP_GROWTH_FACTOR -> populationPolicy.popGrowthFactor(overrideValue);
                            case EDUCATION_EFFICIENCY -> educationPolicy.educationEfficiency(overrideValue);
                            case MIGRATION_PULL -> populationPolicy.migrationPull(overrideValue);
                            case POP_SPENDING -> budgetPolicy.popSpending(overrideValue);
                            case SLAVERY_ALLOWED -> laborPolicy.slaveryAllowed(overrideValue > 0);
                            case POLITICAL_CONSCIOUSNESS -> politicalPolicy.politicalConsciousness(overrideValue);
                            case POLITICAL_RADICALISM -> politicalPolicy.politicalRadicalism(overrideValue);
                            case SUPPRESSION -> politicalPolicy.suppression(overrideValue);
                            case SOCIAL_MOBILITY -> politicalPolicy.socialMobility(overrideValue);
                            case CLASS_RIGIDITY -> politicalPolicy.classRigidity(overrideValue);
                            case ADMINISTRATIVE_EFFICIENCY -> politicalPolicy.administrativeEfficiency(overrideValue);
                            case RELIGIOUS_CONVERSION_SPEED -> culturePolicy.religiousConversionSpeed(overrideValue);
                            case SECULARISM -> culturePolicy.secularism(overrideValue);
                            case ASSIMILATION_RATE -> culturePolicy.assimilationRate(overrideValue);
                            case MIGRATION_PUSH -> populationPolicy.migrationPush(overrideValue);
                        }
                    }
                }
            }

            profitDistributionPolicy.capitalistProfitShareRate(Math.max(0f, profitDistributionPolicy.capitalistProfitShareRate()));
            profitDistributionPolicy.workerProfitShareRate(Math.max(0f, profitDistributionPolicy.workerProfitShareRate()));
            profitDistributionPolicy.aristocratProfitShareRate(Math.max(0f, profitDistributionPolicy.aristocratProfitShareRate()));
            profitDistributionPolicy.stateProfitShareRate(Math.max(0f, profitDistributionPolicy.stateProfitShareRate()));
        }
    }
}
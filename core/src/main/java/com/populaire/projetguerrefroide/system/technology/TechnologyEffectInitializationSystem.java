package com.populaire.projetguerrefroide.system.technology;

import com.populaire.projetguerrefroide.component.*;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;

import static com.populaire.projetguerrefroide.simulation.Constants.*;

public class TechnologyEffectInitializationSystem {

    public TechnologyEffectInitializationSystem(World ecsWorld, long phaseId) {
        long[] technologyIds = new long[MAX_TECHNOLOGIES];
        Query technologyQuery = ecsWorld.query().with(Technology.class).build();
        technologyQuery.each(entityId -> {
            TechnologyView technology = ecsWorld.obtainEntityView(entityId).getView(Technology.class);
            technologyIds[technology.index()] = entityId;
        });
        technologyQuery.destruct();

        ecsWorld.system("TechnologyEffectInitializationSystem")
            .kind(phaseId)
            .with(Country.class)
            .with(CountryTechnology.class)
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
            .iter(iter -> this.initialize(iter, technologyIds));
    }

    private void initialize(Iter iter, long[] technologyIds) {
        Field<CountryTechnology> countryTechnologyField = iter.field(CountryTechnology.class, 1);
        Field<CountryTaxPolicy> taxPolicyField = iter.field(CountryTaxPolicy.class, 2);
        Field<CountryBudgetPolicy> budgetPolicyField = iter.field(CountryBudgetPolicy.class, 3);
        Field<CountryEducationPolicy> educationPolicyField = iter.field(CountryEducationPolicy.class, 4);
        Field<CountryTradePolicy> tradePolicyField = iter.field(CountryTradePolicy.class, 5);
        Field<CountryProductionPolicy> productionPolicyField = iter.field(CountryProductionPolicy.class, 6);
        Field<CountryLaborPolicy> laborPolicyField = iter.field(CountryLaborPolicy.class, 7);
        Field<CountryPopulationPolicy> populationPolicyField = iter.field(CountryPopulationPolicy.class, 8);
        Field<CountryPoliticalPolicy> politicalPolicyField = iter.field(CountryPoliticalPolicy.class, 9);
        Field<CountryCulturePolicy> culturePolicyField = iter.field(CountryCulturePolicy.class, 10);
        Field<CountryProfitDistributionPolicy> profitDistributionPolicyField = iter.field(CountryProfitDistributionPolicy.class, 11);

        for (int i = 0; i < iter.count(); i++) {
            CountryTechnologyView countryTechnology = countryTechnologyField.getMutView(i);
            CountryTaxPolicyMutView taxPolicy = taxPolicyField.getMutView(i);
            CountryBudgetPolicyMutView budgetPolicy = budgetPolicyField.getMutView(i);
            CountryEducationPolicyMutView educationPolicy = educationPolicyField.getMutView(i);
            CountryTradePolicyMutView tradePolicy = tradePolicyField.getMutView(i);
            CountryProductionPolicyMutView productionPolicy = productionPolicyField.getMutView(i);
            CountryLaborPolicyMutView laborPolicy = laborPolicyField.getMutView(i);
            CountryPopulationPolicyMutView populationPolicy = populationPolicyField.getMutView(i);
            CountryPoliticalPolicyMutView politicalPolicy = politicalPolicyField.getMutView(i);
            CountryCulturePolicyMutView culturePolicy = culturePolicyField.getMutView(i);
            CountryProfitDistributionPolicyMutView profitDistributionPolicy = profitDistributionPolicyField.getMutView(i);

            for (int t = 0; t < countryTechnology.acquiredLength(); t++) {
                if (!countryTechnology.acquired(t)) {
                    continue;
                }
                long technologyId = technologyIds[t];
                if (technologyId == 0) {
                    continue;
                }
                EntityView technology = iter.stage().obtainEntityView(technologyId);
                if (!technology.has(Modifiers.class)) {
                    continue;
                }
                ModifiersView technologyModifiers = technology.getMutView(Modifiers.class);
                for (int m = 0; m < technologyModifiers.tagIdsLength(); m++) {
                    long modifierId = technologyModifiers.tagIds(m);
                    if (modifierId <= 0) {
                        break;
                    }
                    float modifierValue = technologyModifiers.values(m);
                    EntityView modifier = iter.stage().obtainEntityView(modifierId);
                    String modifierName = modifier.name();

                    switch (modifierName) {
                        case TAX_POOR -> taxPolicy.poorTaxRate(taxPolicy.poorTaxRate() + modifierValue);
                        case TAX_MIDDLE -> taxPolicy.middleTaxRate(taxPolicy.middleTaxRate() + modifierValue);
                        case TAX_RICH -> taxPolicy.richTaxRate(taxPolicy.richTaxRate() + modifierValue);
                        case RESOURCE_TAX -> taxPolicy.resourceTaxRate(taxPolicy.resourceTaxRate() + modifierValue);
                        case PROFIT_TAX -> taxPolicy.profitTaxRate(taxPolicy.profitTaxRate() + modifierValue);
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
                        case STATE_CONSTRUCTION -> productionPolicy.stateConstruction(productionPolicy.stateConstruction() + modifierValue);
                        case POP_GROWTH_FACTOR -> populationPolicy.popGrowthFactor(populationPolicy.popGrowthFactor() + modifierValue);
                        case EDUCATION_EFFICIENCY -> educationPolicy.educationEfficiency(educationPolicy.educationEfficiency() + modifierValue);
                        case RESEARCH_EFFICIENCY -> educationPolicy.researchEfficiency(educationPolicy.researchEfficiency() + modifierValue);
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
                        case RELIGIOUS_DONATIONS -> culturePolicy.religiousDonationRate(culturePolicy.religiousDonationRate() + modifierValue);
                        case CLERGY_STATE_FUNDING -> budgetPolicy.clergyFundingRate(budgetPolicy.clergyFundingRate() + modifierValue);
                        case WORKFORCE_SHARE -> populationPolicy.workforceShareModifier(populationPolicy.workforceShareModifier() + modifierValue);
                        case INDUSTRY_SUBSIDIES -> productionPolicy.industrySubsidyRate(productionPolicy.industrySubsidyRate() + modifierValue);
                        case PUBLIC_HEALTH -> budgetPolicy.publicHealthRate(budgetPolicy.publicHealthRate() + modifierValue);
                        case PUBLIC_WORKS -> productionPolicy.publicWorksModifier(productionPolicy.publicWorksModifier() + modifierValue);
                        case PENSION -> budgetPolicy.pensionRate(budgetPolicy.pensionRate() + modifierValue);
                    }
                }
            }

            taxPolicy.poorTaxRate(Math.max(0f, taxPolicy.poorTaxRate()));
            taxPolicy.middleTaxRate(Math.max(0f, taxPolicy.middleTaxRate()));
            taxPolicy.richTaxRate(Math.max(0f, taxPolicy.richTaxRate()));
            taxPolicy.resourceTaxRate(Math.clamp(taxPolicy.resourceTaxRate(), 0f, 1f));
            taxPolicy.profitTaxRate(Math.clamp(taxPolicy.profitTaxRate(), 0f, 1f));
            profitDistributionPolicy.capitalistProfitShareRate(Math.max(0f, profitDistributionPolicy.capitalistProfitShareRate()));
            profitDistributionPolicy.workerProfitShareRate(Math.max(0f, profitDistributionPolicy.workerProfitShareRate()));
            profitDistributionPolicy.aristocratProfitShareRate(Math.max(0f, profitDistributionPolicy.aristocratProfitShareRate()));
            profitDistributionPolicy.stateProfitShareRate(Math.max(0f, profitDistributionPolicy.stateProfitShareRate()));
            culturePolicy.religiousDonationRate(Math.max(0f, culturePolicy.religiousDonationRate()));
            budgetPolicy.clergyFundingRate(Math.max(0f, budgetPolicy.clergyFundingRate()));
            populationPolicy.workforceShareModifier(Math.max(-1f, populationPolicy.workforceShareModifier()));
            productionPolicy.industrySubsidyRate(Math.clamp(productionPolicy.industrySubsidyRate(), 0f, 1f));
            budgetPolicy.publicHealthRate(Math.clamp(budgetPolicy.publicHealthRate(), 0f, 1f));
            productionPolicy.publicWorksModifier(Math.max(-1f, productionPolicy.publicWorksModifier()));
            budgetPolicy.pensionRate(Math.max(0f, budgetPolicy.pensionRate()));
        }
    }
}

package com.populaire.projetguerrefroide.system.technology;

import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;
import io.github.elebras1.flecs.EntityView;

public final class TechnologyEffectUtils {

    private TechnologyEffectUtils() {
    }

    /** Adds the policy modifiers and the production effects of one acquired technology to its country. */
    public static void applyTechnology(EcsConstants ecsConstants, EntityView country, int technology) {
        int[] modifiers = ecsConstants.technologyModifiers()[technology];
        float[] values = ecsConstants.technologyModifierValues()[technology];
        for (int m = 0; m < modifiers.length; m++) {
            applyModifier(country, PolicyModifier.of(modifiers[m]), values[m]);
        }
        CountryProductionTechnologyMutView production = country.getMutView(CountryProductionTechnology.class);
        int[] rgoGoods = ecsConstants.technologyRgoGoods()[technology];
        float[] rgoProductivities = ecsConstants.technologyRgoProductivities()[technology];
        for (int g = 0; g < rgoGoods.length; g++) {
            production.rgoProductivities(rgoGoods[g], production.rgoProductivities(rgoGoods[g]) + rgoProductivities[g]);
        }
        int[] factoryTypes = ecsConstants.technologyFactoryTypes()[technology];
        float[] factoryProductivities = ecsConstants.technologyFactoryProductivities()[technology];
        for (int f = 0; f < factoryTypes.length; f++) {
            production.factoryProductivities(factoryTypes[f], production.factoryProductivities(factoryTypes[f]) + factoryProductivities[f]);
        }
        int[] mechanizationGoods = ecsConstants.technologyMechanizationGoods()[technology];
        int[] mechanizationCaps = ecsConstants.technologyMechanizationCaps()[technology];
        for (int m = 0; m < mechanizationGoods.length; m++) {
            production.mechanizationCaps(mechanizationGoods[m], production.mechanizationCaps(mechanizationGoods[m]) + mechanizationCaps[m]);
        }
        for (int recipeType : ecsConstants.technologyRecipeTypes()[technology]) {
            production.advancedRecipes(recipeType, true);
        }
    }

    private static void applyModifier(EntityView country, PolicyModifier modifier, float value) {
        switch (modifier) {
            case POOR_TAX -> {
                CountryTaxPolicyMutView policy = country.getMutView(CountryTaxPolicy.class);
                policy.poorTaxRate(policy.poorTaxRate() + value);
            }
            case MIDDLE_TAX -> {
                CountryTaxPolicyMutView policy = country.getMutView(CountryTaxPolicy.class);
                policy.middleTaxRate(policy.middleTaxRate() + value);
            }
            case RICH_TAX -> {
                CountryTaxPolicyMutView policy = country.getMutView(CountryTaxPolicy.class);
                policy.richTaxRate(policy.richTaxRate() + value);
            }
            case RESOURCE_TAX_RATE -> {
                CountryTaxPolicyMutView policy = country.getMutView(CountryTaxPolicy.class);
                policy.resourceTaxRate(policy.resourceTaxRate() + value);
            }
            case PROFIT_TAX_RATE -> {
                CountryTaxPolicyMutView policy = country.getMutView(CountryTaxPolicy.class);
                policy.profitTaxRate(policy.profitTaxRate() + value);
            }
            case TARIFF -> {
                CountryTradePolicyMutView policy = country.getMutView(CountryTradePolicy.class);
                policy.tariffRate(policy.tariffRate() + value);
            }
            case MILITARY -> {
                CountryBudgetPolicyMutView policy = country.getMutView(CountryBudgetPolicy.class);
                policy.militarySpendingRate(policy.militarySpendingRate() + value);
            }
            case EDUCATION -> {
                CountryEducationPolicyMutView policy = country.getMutView(CountryEducationPolicy.class);
                policy.educationSpendingRate(policy.educationSpendingRate() + value);
            }
            case ADMINISTRATION -> {
                CountryBudgetPolicyMutView policy = country.getMutView(CountryBudgetPolicy.class);
                policy.administrationSpendingRate(policy.administrationSpendingRate() + value);
            }
            case SOCIAL -> {
                CountryBudgetPolicyMutView policy = country.getMutView(CountryBudgetPolicy.class);
                policy.socialSpendingRate(policy.socialSpendingRate() + value);
            }
            case CAPITALIST_SHARE -> {
                CountryProfitDistributionPolicyMutView policy = country.getMutView(CountryProfitDistributionPolicy.class);
                policy.capitalistProfitShareRate(policy.capitalistProfitShareRate() + value);
            }
            case WORKER_SHARE -> {
                CountryProfitDistributionPolicyMutView policy = country.getMutView(CountryProfitDistributionPolicy.class);
                policy.workerProfitShareRate(policy.workerProfitShareRate() + value);
            }
            case ARISTOCRAT_SHARE -> {
                CountryProfitDistributionPolicyMutView policy = country.getMutView(CountryProfitDistributionPolicy.class);
                policy.aristocratProfitShareRate(policy.aristocratProfitShareRate() + value);
            }
            case STATE_SHARE -> {
                CountryProfitDistributionPolicyMutView policy = country.getMutView(CountryProfitDistributionPolicy.class);
                policy.stateProfitShareRate(policy.stateProfitShareRate() + value);
            }
            case MIN_WAGE -> {
                CountryLaborPolicyMutView policy = country.getMutView(CountryLaborPolicy.class);
                policy.minWageFactor(policy.minWageFactor() + value);
            }
            case FACTORY_INPUT -> {
                CountryProductionPolicyMutView policy = country.getMutView(CountryProductionPolicy.class);
                policy.factoryInputModifier(policy.factoryInputModifier() + value);
            }
            case FACTORY_OUTPUT -> {
                CountryProductionPolicyMutView policy = country.getMutView(CountryProductionPolicy.class);
                policy.factoryOutputModifier(policy.factoryOutputModifier() + value);
            }
            case RGO_OUTPUT -> {
                CountryProductionPolicyMutView policy = country.getMutView(CountryProductionPolicy.class);
                policy.rgoOutputModifier(policy.rgoOutputModifier() + value);
            }
            case CONSTRUCTION -> {
                CountryProductionPolicyMutView policy = country.getMutView(CountryProductionPolicy.class);
                policy.constructionSpeed(policy.constructionSpeed() + value);
            }
            case STATE_BUILDING -> {
                CountryProductionPolicyMutView policy = country.getMutView(CountryProductionPolicy.class);
                policy.stateConstruction(policy.stateConstruction() + value);
            }
            case POP_GROWTH -> {
                CountryPopulationPolicyMutView policy = country.getMutView(CountryPopulationPolicy.class);
                policy.popGrowthFactor(policy.popGrowthFactor() + value);
            }
            case EDUCATION_EFFICIENCY_RATE -> {
                CountryEducationPolicyMutView policy = country.getMutView(CountryEducationPolicy.class);
                policy.educationEfficiency(policy.educationEfficiency() + value);
            }
            case RESEARCH_EFFICIENCY_RATE -> {
                CountryEducationPolicyMutView policy = country.getMutView(CountryEducationPolicy.class);
                policy.researchEfficiency(policy.researchEfficiency() + value);
            }
            case MIGRATION_PULL_RATE -> {
                CountryPopulationPolicyMutView policy = country.getMutView(CountryPopulationPolicy.class);
                policy.migrationPull(policy.migrationPull() + value);
            }
            case POP_SPENDING_RATE -> {
                CountryBudgetPolicyMutView policy = country.getMutView(CountryBudgetPolicy.class);
                policy.popSpending(policy.popSpending() + value);
            }
            case CONSCIOUSNESS -> {
                CountryPoliticalPolicyMutView policy = country.getMutView(CountryPoliticalPolicy.class);
                policy.politicalConsciousness(policy.politicalConsciousness() + value);
            }
            case RADICALISM -> {
                CountryPoliticalPolicyMutView policy = country.getMutView(CountryPoliticalPolicy.class);
                policy.politicalRadicalism(policy.politicalRadicalism() + value);
            }
            case SUPPRESSION_RATE -> {
                CountryPoliticalPolicyMutView policy = country.getMutView(CountryPoliticalPolicy.class);
                policy.suppression(policy.suppression() + value);
            }
            case MOBILITY -> {
                CountryPoliticalPolicyMutView policy = country.getMutView(CountryPoliticalPolicy.class);
                policy.socialMobility(policy.socialMobility() + value);
            }
            case RIGIDITY -> {
                CountryPoliticalPolicyMutView policy = country.getMutView(CountryPoliticalPolicy.class);
                policy.classRigidity(policy.classRigidity() + value);
            }
            case ADMINISTRATIVE -> {
                CountryPoliticalPolicyMutView policy = country.getMutView(CountryPoliticalPolicy.class);
                policy.administrativeEfficiency(policy.administrativeEfficiency() + value);
            }
            case CONVERSION -> {
                CountryCulturePolicyMutView policy = country.getMutView(CountryCulturePolicy.class);
                policy.religiousConversionSpeed(policy.religiousConversionSpeed() + value);
            }
            case SECULARISM_RATE -> {
                CountryCulturePolicyMutView policy = country.getMutView(CountryCulturePolicy.class);
                policy.secularism(policy.secularism() + value);
            }
            case ASSIMILATION -> {
                CountryCulturePolicyMutView policy = country.getMutView(CountryCulturePolicy.class);
                policy.assimilationRate(policy.assimilationRate() + value);
            }
            case MIGRATION_PUSH_RATE -> {
                CountryPopulationPolicyMutView policy = country.getMutView(CountryPopulationPolicy.class);
                policy.migrationPush(policy.migrationPush() + value);
            }
            case DONATIONS -> {
                CountryCulturePolicyMutView policy = country.getMutView(CountryCulturePolicy.class);
                policy.religiousDonationRate(policy.religiousDonationRate() + value);
            }
            case CLERGY_FUNDING -> {
                CountryBudgetPolicyMutView policy = country.getMutView(CountryBudgetPolicy.class);
                policy.clergyFundingRate(policy.clergyFundingRate() + value);
            }
            case WORKFORCE -> {
                CountryPopulationPolicyMutView policy = country.getMutView(CountryPopulationPolicy.class);
                policy.workforceShareModifier(policy.workforceShareModifier() + value);
            }
            case SUBSIDIES -> {
                CountryProductionPolicyMutView policy = country.getMutView(CountryProductionPolicy.class);
                policy.industrySubsidyRate(policy.industrySubsidyRate() + value);
            }
            case HEALTH -> {
                CountryBudgetPolicyMutView policy = country.getMutView(CountryBudgetPolicy.class);
                policy.publicHealthRate(policy.publicHealthRate() + value);
            }
            case WORKS -> {
                CountryProductionPolicyMutView policy = country.getMutView(CountryProductionPolicy.class);
                policy.publicWorksModifier(policy.publicWorksModifier() + value);
            }
            case PENSIONS -> {
                CountryBudgetPolicyMutView policy = country.getMutView(CountryBudgetPolicy.class);
                policy.pensionRate(policy.pensionRate() + value);
            }
        }
    }

    /** Bounds kept by the policies once technologies are added (same bounds as at initialization). */
    public static void clampPolicies(EntityView country) {
        CountryTaxPolicyMutView taxPolicy = country.getMutView(CountryTaxPolicy.class);
        taxPolicy.poorTaxRate(Math.max(0f, taxPolicy.poorTaxRate()));
        taxPolicy.middleTaxRate(Math.max(0f, taxPolicy.middleTaxRate()));
        taxPolicy.richTaxRate(Math.max(0f, taxPolicy.richTaxRate()));
        taxPolicy.resourceTaxRate(Math.clamp(taxPolicy.resourceTaxRate(), 0f, 1f));
        taxPolicy.profitTaxRate(Math.clamp(taxPolicy.profitTaxRate(), 0f, 1f));
        CountryProfitDistributionPolicyMutView profitDistributionPolicy = country.getMutView(CountryProfitDistributionPolicy.class);
        profitDistributionPolicy.capitalistProfitShareRate(Math.max(0f, profitDistributionPolicy.capitalistProfitShareRate()));
        profitDistributionPolicy.workerProfitShareRate(Math.max(0f, profitDistributionPolicy.workerProfitShareRate()));
        profitDistributionPolicy.aristocratProfitShareRate(Math.max(0f, profitDistributionPolicy.aristocratProfitShareRate()));
        profitDistributionPolicy.stateProfitShareRate(Math.max(0f, profitDistributionPolicy.stateProfitShareRate()));
        CountryCulturePolicyMutView culturePolicy = country.getMutView(CountryCulturePolicy.class);
        culturePolicy.religiousDonationRate(Math.max(0f, culturePolicy.religiousDonationRate()));
        CountryBudgetPolicyMutView budgetPolicy = country.getMutView(CountryBudgetPolicy.class);
        budgetPolicy.clergyFundingRate(Math.max(0f, budgetPolicy.clergyFundingRate()));
        budgetPolicy.publicHealthRate(Math.clamp(budgetPolicy.publicHealthRate(), 0f, 1f));
        budgetPolicy.pensionRate(Math.max(0f, budgetPolicy.pensionRate()));
        CountryPopulationPolicyMutView populationPolicy = country.getMutView(CountryPopulationPolicy.class);
        populationPolicy.workforceShareModifier(Math.max(-1f, populationPolicy.workforceShareModifier()));
        CountryProductionPolicyMutView productionPolicy = country.getMutView(CountryProductionPolicy.class);
        productionPolicy.industrySubsidyRate(Math.clamp(productionPolicy.industrySubsidyRate(), 0f, 1f));
        productionPolicy.publicWorksModifier(Math.max(-1f, productionPolicy.publicWorksModifier()));
    }
}

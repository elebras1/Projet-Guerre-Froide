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
            .with(CountryEffectPolicy.class)
            .iter(this::initialize);
    }

    private void initialize(Iter iter) {
        Field<Country> countryField = iter.field(Country.class, 0);
        Field<CountryEffectPolicy> effectPolicyField = iter.field(CountryEffectPolicy.class, 1);
        for (int i = 0; i < iter.count(); i++) {
            CountryView country = countryField.getMutView(i);
            CountryEffectPolicyView effectPolicy = effectPolicyField.getMutView(i);

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
                        String modifierName = modifier.getName();

                        switch (modifierName) {
                            case TAX_POOR -> effectPolicy.poorTaxRate(effectPolicy.poorTaxRate() + modifierValue);
                            case TAX_MIDDLE -> effectPolicy.middleTaxRate(effectPolicy.middleTaxRate() + modifierValue);
                            case TAX_RICH -> effectPolicy.richTaxRate(effectPolicy.richTaxRate() + modifierValue);
                            case TARIFF_RATE -> effectPolicy.tariffRate(effectPolicy.tariffRate() + modifierValue);
                            case MILITARY_SPENDING -> effectPolicy.militarySpendingRate(effectPolicy.militarySpendingRate() + modifierValue);
                            case EDUCATION_SPENDING -> effectPolicy.educationSpendingRate(effectPolicy.educationSpendingRate() + modifierValue);
                            case ADMINISTRATION_SPENDING -> effectPolicy.administrationSpendingRate(effectPolicy.administrationSpendingRate() + modifierValue);
                            case SOCIAL_SPENDING -> effectPolicy.socialSpendingRate(effectPolicy.socialSpendingRate() + modifierValue);
                            case CAPITALIST_PROFIT_SHARE -> effectPolicy.capitalistProfitShareRate(effectPolicy.capitalistProfitShareRate() + modifierValue);
                            case WORKER_PROFIT_SHARE -> effectPolicy.workerProfitShareRate(effectPolicy.workerProfitShareRate() + modifierValue);
                            case ARISTOCRAT_PROFIT_SHARE -> effectPolicy.aristocratProfitShareRate(effectPolicy.aristocratProfitShareRate() + modifierValue);
                            case STATE_PROFIT_SHARE -> effectPolicy.stateProfitShareRate(effectPolicy.stateProfitShareRate() + modifierValue);
                            case MIN_WAGE_FACTOR -> effectPolicy.minWageFactor(effectPolicy.minWageFactor() + modifierValue);
                            case FACTORY_INPUT_MODIFIER -> effectPolicy.factoryInputModifier(effectPolicy.factoryInputModifier() + modifierValue);
                            case FACTORY_OUTPUT_MODIFIER -> effectPolicy.factoryOutputModifier(effectPolicy.factoryOutputModifier() + modifierValue);
                            case RGO_OUTPUT_MODIFIER -> effectPolicy.rgoOutputModifier(effectPolicy.rgoOutputModifier() + modifierValue);
                            case CONSTRUCTION_SPEED -> effectPolicy.constructionSpeed(effectPolicy.constructionSpeed() + modifierValue);
                            case POP_GROWTH_FACTOR -> effectPolicy.popGrowthFactor(effectPolicy.popGrowthFactor() + modifierValue);
                            case EDUCATION_EFFICIENCY -> effectPolicy.educationEfficiency(effectPolicy.educationEfficiency() + modifierValue);
                            case MIGRATION_PULL -> effectPolicy.migrationPull(effectPolicy.migrationPull() + modifierValue);
                            case POP_SPENDING -> effectPolicy.popSpending(effectPolicy.popSpending() + modifierValue);
                            case POLITICAL_CONSCIOUSNESS -> effectPolicy.politicalConsciousness(effectPolicy.politicalConsciousness() + modifierValue);
                            case POLITICAL_RADICALISM -> effectPolicy.politicalRadicalism(effectPolicy.politicalRadicalism() + modifierValue);
                            case SUPPRESSION -> effectPolicy.suppression(effectPolicy.suppression() + modifierValue);
                            case SOCIAL_MOBILITY -> effectPolicy.socialMobility(effectPolicy.socialMobility() + modifierValue);
                            case CLASS_RIGIDITY -> effectPolicy.classRigidity(effectPolicy.classRigidity() + modifierValue);
                            case ADMINISTRATIVE_EFFICIENCY -> effectPolicy.administrativeEfficiency(effectPolicy.administrativeEfficiency() + modifierValue);
                            case RELIGIOUS_CONVERSION_SPEED -> effectPolicy.religiousConversionSpeed(effectPolicy.religiousConversionSpeed() + modifierValue);
                            case SECULARISM -> effectPolicy.secularism(effectPolicy.secularism() + modifierValue);
                            case ASSIMILATION_RATE -> effectPolicy.assimilationRate(effectPolicy.assimilationRate() + modifierValue);
                            case MIGRATION_PUSH -> effectPolicy.migrationPush(effectPolicy.migrationPush() + modifierValue);
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
                        String overrideName = override.getName();

                        switch (overrideName) {
                            case TAX_POOR -> effectPolicy.poorTaxRate(overrideValue);
                            case TAX_MIDDLE -> effectPolicy.middleTaxRate(overrideValue);
                            case TAX_RICH -> effectPolicy.richTaxRate(overrideValue);
                            case TARIFF_RATE -> effectPolicy.tariffRate(overrideValue);
                            case MILITARY_SPENDING -> effectPolicy.militarySpendingRate(overrideValue);
                            case EDUCATION_SPENDING -> effectPolicy.educationSpendingRate(overrideValue);
                            case ADMINISTRATION_SPENDING -> effectPolicy.administrationSpendingRate(overrideValue);
                            case SOCIAL_SPENDING -> effectPolicy.socialSpendingRate(overrideValue);
                            case CAPITALIST_PROFIT_SHARE -> effectPolicy.capitalistProfitShareRate(overrideValue);
                            case WORKER_PROFIT_SHARE -> effectPolicy.workerProfitShareRate(overrideValue);
                            case ARISTOCRAT_PROFIT_SHARE -> effectPolicy.aristocratProfitShareRate(overrideValue);
                            case STATE_PROFIT_SHARE -> effectPolicy.stateProfitShareRate(overrideValue);
                            case MIN_WAGE_FACTOR -> effectPolicy.minWageFactor(overrideValue);
                            case FACTORY_INPUT_MODIFIER -> effectPolicy.factoryInputModifier(overrideValue);
                            case FACTORY_OUTPUT_MODIFIER -> effectPolicy.factoryOutputModifier(overrideValue);
                            case RGO_OUTPUT_MODIFIER -> effectPolicy.rgoOutputModifier(overrideValue);
                            case CONSTRUCTION_SPEED -> effectPolicy.constructionSpeed(overrideValue);
                            case POP_GROWTH_FACTOR -> effectPolicy.popGrowthFactor(overrideValue);
                            case EDUCATION_EFFICIENCY -> effectPolicy.educationEfficiency(overrideValue);
                            case MIGRATION_PULL -> effectPolicy.migrationPull(overrideValue);
                            case POP_SPENDING -> effectPolicy.popSpending(overrideValue);
                            case SLAVERY_ALLOWED -> effectPolicy.slaveryAllowed(overrideValue > 0);
                            case POLITICAL_CONSCIOUSNESS -> effectPolicy.politicalConsciousness(overrideValue);
                            case POLITICAL_RADICALISM -> effectPolicy.politicalRadicalism(overrideValue);
                            case SUPPRESSION -> effectPolicy.suppression(overrideValue);
                            case SOCIAL_MOBILITY -> effectPolicy.socialMobility(overrideValue);
                            case CLASS_RIGIDITY -> effectPolicy.classRigidity(overrideValue);
                            case ADMINISTRATIVE_EFFICIENCY -> effectPolicy.administrativeEfficiency(overrideValue);
                            case RELIGIOUS_CONVERSION_SPEED -> effectPolicy.religiousConversionSpeed(overrideValue);
                            case SECULARISM -> effectPolicy.secularism(overrideValue);
                            case ASSIMILATION_RATE -> effectPolicy.assimilationRate(overrideValue);
                            case MIGRATION_PUSH -> effectPolicy.migrationPush(overrideValue);
                        }
                    }
                }
            }
        }
    }
}

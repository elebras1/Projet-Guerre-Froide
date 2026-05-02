package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.Constants.*;

public class LawEffectInitializationSystem {

    public LawEffectInitializationSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("LawEffectInitializationSystem")
            .kind(phaseId)
            .with(Country.class)
            .with(CountryEconomicPolicy.class)
            .iter(this::initialize);
    }

    private void initialize(Iter iter) {
        Field<Country> countryField = iter.field(Country.class, 0);
        Field<CountryEconomicPolicy> economicPolicyField = iter.field(CountryEconomicPolicy.class, 1);
        for (int i = 0; i < iter.count(); i++) {
            CountryView country = countryField.getMutView(i);
            CountryEconomicPolicyView economicPolicy = economicPolicyField.getMutView(i);
            for (int l = 0; l < country.activeLawIdsLength(); l++) {
                long lawId = country.activeLawIds(l);
                if (lawId == 0) {
                    continue;
                }

                EntityView law = iter.world().obtainEntityView(lawId);

                ModifiersView lawModifiers = law.getMutView(Modifiers.class);
                for (int m = 0; m < lawModifiers.tagIdsLength(); m++) {
                    long modifierId = lawModifiers.tagIds(m);
                    if (modifierId <= 0) {
                        break;
                    }

                    float modifierValue = lawModifiers.values(m);
                    EntityView modifier = iter.world().obtainEntityView(modifierId);
                    String modifierName = modifier.getName();

                    switch (modifierName) {
                        case TAX_POOR -> economicPolicy.poorTaxRate(economicPolicy.poorTaxRate() + modifierValue);
                        case TAX_MIDDLE -> economicPolicy.middleTaxRate(economicPolicy.middleTaxRate() + modifierValue);
                        case TAX_RICH -> economicPolicy.richTaxRate(economicPolicy.richTaxRate() + modifierValue);
                        case TARIFF_RATE -> economicPolicy.tariffRate(economicPolicy.tariffRate() + modifierValue);
                        case MILITARY_SPENDING -> economicPolicy.militarySpendingRate(economicPolicy.militarySpendingRate() + modifierValue);
                        case EDUCATION_SPENDING -> economicPolicy.educationSpendingRate(economicPolicy.educationSpendingRate() + modifierValue);
                        case ADMINISTRATION_SPENDING -> economicPolicy.administrationSpendingRate(economicPolicy.administrationSpendingRate() + modifierValue);
                        case SOCIAL_SPENDING -> economicPolicy.socialSpendingRate(economicPolicy.socialSpendingRate() + modifierValue);
                        case CAPITALIST_PROFIT_SHARE -> economicPolicy.capitalistProfitShare(economicPolicy.capitalistProfitShare() + modifierValue);
                        case WORKER_PROFIT_SHARE -> economicPolicy.workerProfitShare(economicPolicy.workerProfitShare() + modifierValue);
                        case ARISTOCRAT_PROFIT_SHARE -> economicPolicy.aristocratProfitShare(economicPolicy.aristocratProfitShare() + modifierValue);
                        case STATE_PROFIT_SHARE -> economicPolicy.stateProfitShare(economicPolicy.stateProfitShare() + modifierValue);
                        case MIN_WAGE_FACTOR -> economicPolicy.minWageFactor(economicPolicy.minWageFactor() + modifierValue);
                        case FACTORY_INPUT_MODIFIER -> economicPolicy.factoryInputModifier(economicPolicy.factoryInputModifier() + modifierValue);
                        case FACTORY_OUTPUT_MODIFIER -> economicPolicy.factoryOutputModifier(economicPolicy.factoryOutputModifier() + modifierValue);
                        case RGO_OUTPUT_MODIFIER -> economicPolicy.rgoOutputModifier(economicPolicy.rgoOutputModifier() + modifierValue);
                        case CONSTRUCTION_SPEED -> economicPolicy.constructionSpeed(economicPolicy.constructionSpeed() + modifierValue);
                        case POP_GROWTH_FACTOR -> economicPolicy.popGrowthFactor(economicPolicy.popGrowthFactor() + modifierValue);
                        case EDUCATION_EFFICIENCY -> economicPolicy.educationEfficiency(economicPolicy.educationEfficiency() + modifierValue);
                        case MIGRATION_PULL -> economicPolicy.migrationPull(economicPolicy.migrationPull() + modifierValue);
                    }
                }

                OverridesView lawOverrides = law.getMutView(Overrides.class);
                for (int m = 0; m < lawOverrides.tagIdsLength(); m++) {
                    long overrideId = lawOverrides.tagIds(m);
                    if (overrideId <= 0) {
                        break;
                    }

                    float overrideValue = lawOverrides.values(m);
                    EntityView override = iter.world().obtainEntityView(overrideId);
                    String overrideName = override.getName();

                    switch (overrideName) {
                        case TAX_POOR -> economicPolicy.poorTaxRate(overrideValue);
                        case TAX_MIDDLE -> economicPolicy.middleTaxRate(overrideValue);
                        case TAX_RICH -> economicPolicy.richTaxRate(overrideValue);
                        case TARIFF_RATE -> economicPolicy.tariffRate(overrideValue);
                        case MILITARY_SPENDING -> economicPolicy.militarySpendingRate(overrideValue);
                        case EDUCATION_SPENDING -> economicPolicy.educationSpendingRate(overrideValue);
                        case ADMINISTRATION_SPENDING -> economicPolicy.administrationSpendingRate(overrideValue);
                        case SOCIAL_SPENDING -> economicPolicy.socialSpendingRate(overrideValue);
                        case CAPITALIST_PROFIT_SHARE -> economicPolicy.capitalistProfitShare(overrideValue);
                        case WORKER_PROFIT_SHARE -> economicPolicy.workerProfitShare(overrideValue);
                        case ARISTOCRAT_PROFIT_SHARE -> economicPolicy.aristocratProfitShare(overrideValue);
                        case STATE_PROFIT_SHARE -> economicPolicy.stateProfitShare(overrideValue);
                        case MIN_WAGE_FACTOR -> economicPolicy.minWageFactor(overrideValue);
                        case FACTORY_INPUT_MODIFIER -> economicPolicy.factoryInputModifier(overrideValue);
                        case FACTORY_OUTPUT_MODIFIER -> economicPolicy.factoryOutputModifier(overrideValue);
                        case RGO_OUTPUT_MODIFIER -> economicPolicy.rgoOutputModifier(overrideValue);
                        case CONSTRUCTION_SPEED -> economicPolicy.constructionSpeed(overrideValue);
                        case POP_GROWTH_FACTOR -> economicPolicy.popGrowthFactor(overrideValue);
                        case EDUCATION_EFFICIENCY -> economicPolicy.educationEfficiency(overrideValue);
                        case MIGRATION_PULL -> economicPolicy.migrationPull(overrideValue);
                    }
                }
            }

            System.out.println("Country : " + iter.world().obtainEntityView(iter.entityId(i)).getName() + " data : " + economicPolicyField.get(i));
        }
    }
}

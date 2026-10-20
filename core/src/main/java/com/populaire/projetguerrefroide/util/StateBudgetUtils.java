package com.populaire.projetguerrefroide.util;

import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.Constants.*;
import static com.populaire.projetguerrefroide.util.IncomeTypeUtils.*;
import static com.populaire.projetguerrefroide.util.StrataUtils.POOR_STRATA;

public class StateBudgetUtils {

    public static float slider(float spendingRate) {
        return Math.max(0f, 1f + spendingRate);
    }

    public static boolean assisted(int strata, int incomeType) {
        return strata == POOR_STRATA && incomeType == NO_INCOME_TYPE;
    }

    public static void readPopulationTypes(World world, int[] incomeTypes, boolean[] assistedTypes) {
        GlobalPopulationTypeView globalPopulationType = world.obtainEntityView(world.lookup("global_population_type")).getMutView(GlobalPopulationType.class);
        for (int p = 0; p < globalPopulationType.popTypeIdsLength(); p++) {
            PopulationTypeView populationType = world.obtainEntityView(globalPopulationType.popTypeIds(p)).getView(PopulationType.class);
            incomeTypes[p] = populationType.incomeType();
            assistedTypes[p] = assisted(populationType.strata(), populationType.incomeType());
        }
    }

    public static int statePostIndex(World world) {
        GlobalPopulationTypeView globalPopulationType = world.obtainEntityView(world.lookup("global_population_type")).getView(GlobalPopulationType.class);
        return globalPopulationType.statePostPopTypeIndex();
    }

    public static double scaledWageBase(CountryDemographicsView countryDemographics, int popTypeIndex, int statePostIndex) {
        long base = popTypeIndex == statePostIndex ? countryDemographics.employmentByPopType(popTypeIndex) : countryDemographics.totalByPopType(popTypeIndex);
        return (double) base / NEEDS_SCALING_FACTOR;
    }

    public static float wageSlider(int incomeType, CountryBudgetPolicyView budgetPolicy, CountryEducationPolicyView educationPolicy) {
        return switch (incomeType) {
            case ADMINISTRATION_INCOME_TYPE -> slider(budgetPolicy.administrationSpendingRate());
            case MILITARY_INCOME_TYPE -> slider(budgetPolicy.militarySpendingRate());
            case EDUCATION_INCOME_TYPE -> slider(educationPolicy.educationSpendingRate());
            case RELIGIOUS_INCOME_TYPE -> budgetPolicy.clergyFundingRate();
            default -> 0f;
        };
    }

    public static float wageRate(CountryMarketView countryMarket, int popTypeIndex, float wageSlider) {
        return wageSlider * (countryMarket.lifeCostsByPopType(popTypeIndex) + countryMarket.everydayCostsByPopType(popTypeIndex)
            + STATE_LUXURY_WAGE_SHARE * countryMarket.luxuryCostsByPopType(popTypeIndex));
    }

    public static float socialRate(CountryMarketView countryMarket, int popTypeIndex, float socialSlider) {
        return socialSlider * SOCIAL_LIFE_SHARE * countryMarket.lifeCostsByPopType(popTypeIndex);
    }

    public static float scaledUnemployed(CountryDemographicsView countryDemographics, int popTypeIndex) {
        return Math.max(0L, countryDemographics.totalByPopType(popTypeIndex) - countryDemographics.employmentByPopType(popTypeIndex)) / NEEDS_SCALING_FACTOR;
    }

    public static double dailyNeeds(int[] incomeTypes, boolean[] assistedTypes, CountryMarketView countryMarket, CountryDemographicsView countryDemographics,
                                    CountryBudgetPolicyView budgetPolicy, CountryEducationPolicyView educationPolicy, int statePostIndex) {
        float socialSlider = slider(budgetPolicy.socialSpendingRate());
        double needs = 0.0;
        for (int p = 0; p < POP_TYPE_COUNT; p++) {
            if (assistedTypes[p]) {
                needs += (double) scaledUnemployed(countryDemographics, p) * socialRate(countryMarket, p, socialSlider);
            } else {
                needs += scaledWageBase(countryDemographics, p, statePostIndex) * wageRate(countryMarket, p, wageSlider(incomeTypes[p], budgetPolicy, educationPolicy));
            }
        }
        return needs;
    }
}

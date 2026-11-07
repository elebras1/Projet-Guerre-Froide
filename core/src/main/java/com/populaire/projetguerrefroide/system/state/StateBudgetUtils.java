package com.populaire.projetguerrefroide.system.state;

import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.system.population.EmploymentUtils;

import static com.populaire.projetguerrefroide.simulation.Constants.*;
import static com.populaire.projetguerrefroide.system.state.IncomeTypeUtils.*;
import static com.populaire.projetguerrefroide.system.population.StrataUtils.POOR_STRATA;

public class StateBudgetUtils {

    public static float slider(float spendingRate) {
        return Math.max(0f, 1f + spendingRate);
    }

    public static boolean assisted(int strata, int incomeType) {
        return strata == POOR_STRATA && incomeType == NO_INCOME_TYPE;
    }

    public static boolean paidOnPosts(int popTypeIndex, int statePostIndex, int incomeType) {
        return popTypeIndex == statePostIndex || incomeType == MILITARY_INCOME_TYPE;
    }

    public static double scaledWageBase(CountryDemographicsView countryDemographics, int popTypeIndex, int statePostIndex, int incomeType) {
        long base = paidOnPosts(popTypeIndex, statePostIndex, incomeType) ? countryDemographics.employmentByPopType(popTypeIndex) : countryDemographics.totalByPopType(popTypeIndex);
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

    public static float pensionRate(CountryMarketView countryMarket, int popTypeIndex, float pension) {
        return pension * (countryMarket.lifeCostsByPopType(popTypeIndex) + countryMarket.everydayCostsByPopType(popTypeIndex));
    }

    public static float pensionMortality(float pensionCoverage) {
        return (1f + PENSION_MORTALITY_FACTOR) / (1f + PENSION_MORTALITY_FACTOR * pensionCoverage / PENSION_REFERENCE_COVERAGE);
    }

    public static float scaledUnemployed(CountryDemographicsView countryDemographics, int popTypeIndex, float workforceShare) {
        return Math.max(0f, countryDemographics.totalByPopType(popTypeIndex) * workforceShare - countryDemographics.employmentByPopType(popTypeIndex)) / NEEDS_SCALING_FACTOR;
    }

    public static double stateInterestRate(double stateLoans, double creditLimit, float lendingRate) {
        double debtRatio = creditLimit > 0.0 ? Math.min(1.0, stateLoans / creditLimit) : 1.0;
        return lendingRate * (1.0 + debtRatio);
    }

    public static double maintenanceCost(CountryMaintenanceView countryMaintenance, CountryMarketView countryMarket) {
        double cost = 0.0;
        for (int g = 0; g < countryMaintenance.needsLength(); g++) {
            cost += (double) countryMaintenance.needs(g) * countryMarket.goodBudgetPrices(g);
        }
        return cost;
    }

    public static double militarySupplyCost(CountryMilitaryView countryMilitary, CountryMarketView countryMarket) {
        double cost = 0.0;
        for (int g = 0; g < countryMilitary.supplyNeedsLength(); g++) {
            cost += (double) countryMilitary.supplyNeeds(g) * countryMarket.goodBudgetPrices(g);
        }
        return cost;
    }

    public static double dailyNeeds(int[] incomeTypes, boolean[] assistedTypes, CountryMarketView countryMarket, CountryDemographicsView countryDemographics,
                                    CountryBudgetPolicyView budgetPolicy, CountryEducationPolicyView educationPolicy, int statePostIndex,
                                    float[] baseWorkforceShares, float workforceShareModifier) {
        float socialSlider = slider(budgetPolicy.socialSpendingRate());
        double needs = 0.0;
        for (int p = 0; p < POP_TYPE_COUNT; p++) {
            if (assistedTypes[p]) {
                needs += (double) scaledUnemployed(countryDemographics, p, EmploymentUtils.activeShare(baseWorkforceShares[p], workforceShareModifier)) * socialRate(countryMarket, p, socialSlider);
            } else {
                needs += scaledWageBase(countryDemographics, p, statePostIndex, incomeTypes[p]) * wageRate(countryMarket, p, wageSlider(incomeTypes[p], budgetPolicy, educationPolicy));
            }
            needs += (double) countryDemographics.seniorsByPopType(p) / NEEDS_SCALING_FACTOR * pensionRate(countryMarket, p, budgetPolicy.pensionRate());
        }
        return needs;
    }
}

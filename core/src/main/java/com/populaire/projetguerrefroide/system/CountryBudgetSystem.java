package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EmploymentUtils;
import com.populaire.projetguerrefroide.util.StateConsumptionUtils;

import static com.populaire.projetguerrefroide.util.Constants.*;
import static com.populaire.projetguerrefroide.util.StateBudgetUtils.*;

public class CountryBudgetSystem {

    public CountryBudgetSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("CountryBudgetSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .with(CountryDemographics.class)
            .with(CountryBudgetPolicy.class)
            .with(CountryEducationPolicy.class)
            .with(CountryTradePolicy.class)
            .with(CountryBudget.class)
            .with(CountryBank.class)
            .with(CountryInvestment.class)
            .with(CountryPopulationPolicy.class)
            .with(CountryMilitary.class)
            .with(CountryMaintenance.class)
            .multiThreaded()
            .iter(this::budget);
    }

    private void budget(Iter iter) {
        int[] incomeTypes = new int[POP_TYPE_COUNT];
        boolean[] assistedTypes = new boolean[POP_TYPE_COUNT];
        readPopulationTypes(iter.world(), incomeTypes, assistedTypes);
        int statePostIndex = statePostIndex(iter.world());
        float[] baseWorkforceShares = EmploymentUtils.baseWorkforceShares(iter.world());
        GlobalMarketView globalMarket = iter.world().obtainEntityView(iter.world().lookup("global_market")).getMutView(GlobalMarket.class);

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        Field<CountryDemographics> countryDemographicsField = iter.field(CountryDemographics.class, 1);
        Field<CountryBudgetPolicy> countryBudgetPolicyField = iter.field(CountryBudgetPolicy.class, 2);
        Field<CountryEducationPolicy> countryEducationPolicyField = iter.field(CountryEducationPolicy.class, 3);
        Field<CountryTradePolicy> countryTradePolicyField = iter.field(CountryTradePolicy.class, 4);
        Field<CountryBudget> countryBudgetField = iter.field(CountryBudget.class, 5);
        Field<CountryBank> countryBankField = iter.field(CountryBank.class, 6);
        Field<CountryInvestment> countryInvestmentField = iter.field(CountryInvestment.class, 7);
        Field<CountryPopulationPolicy> countryPopulationPolicyField = iter.field(CountryPopulationPolicy.class, 8);
        Field<CountryMilitary> countryMilitaryField = iter.field(CountryMilitary.class, 9);
        Field<CountryMaintenance> countryMaintenanceField = iter.field(CountryMaintenance.class, 10);
        GlobalStateConsumptionView stateConsumption = StateConsumptionUtils.globalStateConsumption(iter.world());
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
            CountryDemographicsView countryDemographics = countryDemographicsField.getMutView(i);
            CountryBudgetPolicyView countryBudgetPolicy = countryBudgetPolicyField.getMutView(i);
            CountryEducationPolicyView countryEducationPolicy = countryEducationPolicyField.getMutView(i);
            CountryTradePolicyView countryTradePolicy = countryTradePolicyField.getMutView(i);
            CountryBudgetMutView countryBudget = countryBudgetField.getMutView(i);
            CountryBankMutView countryBank = countryBankField.getMutView(i);
            CountryInvestmentMutView countryInvestment = countryInvestmentField.getMutView(i);
            CountryPopulationPolicyView countryPopulationPolicy = countryPopulationPolicyField.getMutView(i);
            float workforceShareModifier = countryPopulationPolicy.workforceShareModifier();
            CountryMilitaryView countryMilitary = countryMilitaryField.getMutView(i);
            CountryMaintenanceView countryMaintenance = countryMaintenanceField.getMutView(i);

            double subsidies = 0.0;
            float tariffRate = countryTradePolicy.tariffRate();
            if (tariffRate < 0f) {
                for (int g = 0; g < countryMarket.goodImportedAmountsLength(); g++) {
                    subsidies += (double) countryMarket.goodImportedAmounts(g) * globalMarket.goodPrices(g) * -tariffRate;
                }
            }

            double stateConsumptionCost = StateConsumptionUtils.dailyCost(stateConsumption, countryMarket,
                StateConsumptionUtils.administrationUnits(countryDemographics, statePostIndex, countryBudgetPolicy),
                StateConsumptionUtils.publicHealthUnits(countryDemographics, countryBudgetPolicy));
            double militarySupplyCost = militarySupplyCost(countryMilitary, countryMarket);
            double maintenanceCost = maintenanceCost(countryMaintenance, countryMarket);
            double needs = dailyNeeds(incomeTypes, assistedTypes, countryMarket, countryDemographics, countryBudgetPolicy, countryEducationPolicy, statePostIndex, baseWorkforceShares, workforceShareModifier)
                + subsidies + stateConsumptionCost + militarySupplyCost + maintenanceCost;
            double revenue = countryMarket.taxRevenue() + countryMarket.tariffRevenue() + countryMarket.profitRevenue();
            double revenueTrend = countryBudget.revenueTrend() > 0.0 ? countryBudget.revenueTrend() + (revenue - countryBudget.revenueTrend()) / DAYS_PER_YEAR : revenue;
            double creditLimit = CREDIT_LIMIT_YEARS * DAYS_PER_YEAR * revenueTrend;
            double stateLoans = countryBank.loans(STATE_OWNER);
            double interest = stateLoans * stateInterestRate(stateLoans, creditLimit, countryBank.lendingRate()) / DAYS_PER_YEAR;
            double interestPaid = Math.min(interest, Math.max(0.0, countryMarket.treasury()));
            if (interestPaid > 0.0) {
                countryMarket.treasury(countryMarket.treasury() - interestPaid);
                countryBank.interestPool(countryBank.interestPool() + interestPaid);
                countryBank.interestPaid(STATE_OWNER, countryBank.interestPaid(STATE_OWNER) + interestPaid);
            }
            double unpaidInterest = interest - interestPaid;
            if (unpaidInterest > 0.0) {
                if (unpaidInterest <= Math.min(Math.max(0.0, countryBank.reserves()), Math.max(0.0, creditLimit - stateLoans))) {
                    stateLoans += unpaidInterest;
                    countryBank.reserves(countryBank.reserves() - unpaidInterest);
                    countryBank.borrowed(STATE_OWNER, countryBank.borrowed(STATE_OWNER) + unpaidInterest);
                    countryBank.interestPool(countryBank.interestPool() + unpaidInterest);
                    countryBank.interestPaid(STATE_OWNER, countryBank.interestPaid(STATE_OWNER) + unpaidInterest);
                } else {
                    countryBank.losses(countryBank.losses() + stateLoans);
                    countryBank.writtenOff(STATE_OWNER, countryBank.writtenOff(STATE_OWNER) + stateLoans);
                    countryBank.bankruptcies(countryBank.bankruptcies() + 1);
                    stateLoans = 0.0;
                }
            }
            double treasury = Math.max(0.0, countryMarket.treasury());
            double loan = needs > treasury
                ? Math.min(needs - treasury, Math.min(Math.max(0.0, countryBank.reserves()), Math.max(0.0, creditLimit - stateLoans))) : 0.0;
            if (loan > 0.0) {
                countryBank.reserves(countryBank.reserves() - loan);
                countryMarket.treasury(countryMarket.treasury() + loan);
                countryBank.borrowed(STATE_OWNER, countryBank.borrowed(STATE_OWNER) + loan);
                stateLoans += loan;
                treasury += loan;
            }
            float spendingRatio = needs > treasury ? (float) (treasury / needs) : 1f;
            double targetTreasury = TARGET_TREASURY_DAYS * needs;
            double excess = treasury - needs - targetTreasury;

            float socialSlider = slider(countryBudgetPolicy.socialSpendingRate());
            double wagesBudget = 0.0;
            double socialBudget = 0.0;
            for (int p = 0; p < POP_TYPE_COUNT; p++) {
                float popTypeWageRate = 0f;
                float popTypeSocialRate = 0f;
                if (assistedTypes[p]) {
                    popTypeSocialRate = spendingRatio * socialRate(countryMarket, p, socialSlider);
                    socialBudget += (double) scaledUnemployed(countryDemographics, p, EmploymentUtils.activeShare(baseWorkforceShares[p], workforceShareModifier)) * popTypeSocialRate;
                } else {
                    popTypeWageRate = spendingRatio * wageRate(countryMarket, p, wageSlider(incomeTypes[p], countryBudgetPolicy, countryEducationPolicy));
                    wagesBudget += scaledWageBase(countryDemographics, p, statePostIndex, incomeTypes[p]) * popTypeWageRate;
                }
                countryBudget.wageRatesByPopType(p, popTypeWageRate);
                countryBudget.socialRatesByPopType(p, popTypeSocialRate);
            }

            double investableSurplus = Math.max(0.0, excess - countryInvestment.committed(STATE_OWNER));
            double repaid = Math.min(stateLoans, investableSurplus);
            if (repaid > 0.0) {
                countryMarket.treasury(countryMarket.treasury() - repaid);
                countryBank.reserves(countryBank.reserves() + repaid);
                countryBank.repaid(STATE_OWNER, countryBank.repaid(STATE_OWNER) + repaid);
                stateLoans -= repaid;
                investableSurplus -= repaid;
            }
            countryBank.loans(STATE_OWNER, stateLoans);
            countryBank.creditLimit(creditLimit);

            countryBudget.targetTreasury(targetTreasury);
            countryBudget.reservedNeeds(needs);
            countryBudget.investableSurplus(investableSurplus);
            countryBudget.revenueTrend(revenueTrend);
            countryBudget.spendingRatio(spendingRatio);
            countryBudget.subsidyAllowance((float) (spendingRatio * subsidies));
            countryBudget.wagesBudget((float) wagesBudget);
            countryBudget.socialBudget((float) socialBudget);
            countryBudget.stateConsumptionBudget((float) (spendingRatio * stateConsumptionCost));
            countryBudget.militarySupplyRatio(spendingRatio);
        }
    }
}

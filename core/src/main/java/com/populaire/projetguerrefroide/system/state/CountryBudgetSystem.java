package com.populaire.projetguerrefroide.system.state;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.system.population.EmploymentUtils;
import com.populaire.projetguerrefroide.simulation.EcsConstants;

import static com.populaire.projetguerrefroide.simulation.Constants.*;
import static com.populaire.projetguerrefroide.system.state.StateBudgetUtils.*;

public class CountryBudgetSystem {
    private final EcsConstants ecsConstants;

    public CountryBudgetSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
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
        int[] incomeTypes = this.ecsConstants.incomeTypes();
        boolean[] assistedTypes = this.ecsConstants.assistedTypes();
        int statePostIndex = this.ecsConstants.statePostIndex();
        float[] baseWorkforceShares = this.ecsConstants.baseWorkforceShares();
        GlobalMarketView globalMarket = iter.stage().obtainEntityView(this.ecsConstants.globalMarket()).getMutView(GlobalMarket.class);
        int day = iter.stage().obtainEntityView(this.ecsConstants.economyCalendar()).<EconomyCalendarView>getView(EconomyCalendar.class).day();
        // the revenue trend is the mean of the days seen during the first year, then a one-year moving average (annex 14,
        // decision 228): anchored on the first day alone, it stayed a year below the revenue (tariffs start at zero)
        double trendWeight = Math.min(DAYS_PER_YEAR, day + 1.0);

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
        GlobalStateConsumptionView stateConsumption = StateConsumptionUtils.globalStateConsumption(iter.stage(), this.ecsConstants);
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

            double administrationConsumptionCost = StateConsumptionUtils.dailyCost(stateConsumption, countryMarket,
                StateConsumptionUtils.administrationUnits(countryDemographics, statePostIndex, countryBudgetPolicy), 0f);
            double healthConsumptionCost = StateConsumptionUtils.dailyCost(stateConsumption, countryMarket, 0f,
                StateConsumptionUtils.publicHealthUnits(countryDemographics, countryBudgetPolicy));
            double stateConsumptionCost = administrationConsumptionCost + healthConsumptionCost;
            double militarySupplyCost = militarySupplyCost(countryMilitary, countryMarket);
            double maintenanceCost = maintenanceCost(countryMaintenance, countryMarket);
            double needs = dailyNeeds(incomeTypes, assistedTypes, countryMarket, countryDemographics, countryBudgetPolicy, countryEducationPolicy, statePostIndex, baseWorkforceShares, workforceShareModifier)
                + subsidies + stateConsumptionCost + militarySupplyCost + maintenanceCost;
            double revenue = countryMarket.taxRevenue() + countryMarket.tariffRevenue() + countryMarket.profitRevenue();
            double revenueTrend = countryBudget.revenueTrend() > 0.0 ? countryBudget.revenueTrend() + (revenue - countryBudget.revenueTrend()) / trendWeight : revenue;
            double creditLimit = CREDIT_LIMIT_YEARS * DAYS_PER_YEAR * revenueTrend;
            // public posts within the state's revenue (annex 14, decision 228): what the yearly revenue leaves after every
            // other need pays at most this many state posts, wage and supplies; StatePostSystem opens no more posts the next
            // day (negative = no limit: no revenue known yet, or fewer than 30 days of it)
            double postWage = statePostIndex >= 0 ? wageRate(countryMarket, statePostIndex, wageSlider(incomeTypes[statePostIndex], countryBudgetPolicy, countryEducationPolicy)) : 0.0;
            double postSupplies = statePostIndex >= 0 ? StateConsumptionUtils.dailyCost(stateConsumption, countryMarket, slider(countryBudgetPolicy.administrationSpendingRate()), 0f) : 0.0;
            double postNeeds = (statePostIndex >= 0 ? scaledWageBase(countryDemographics, statePostIndex, statePostIndex, incomeTypes[statePostIndex]) * postWage : 0.0)
                + administrationConsumptionCost;
            double otherNeeds = needs - postNeeds;
            countryBudget.statePostNeeds((float) postNeeds);
            countryBudget.statePostCapacity(day >= STATE_POST_CAPACITY_WARMUP_DAYS && revenueTrend > 0.0 && postWage + postSupplies > 0.0
                ? (float) (Math.max(0.0, revenueTrend - otherNeeds) / (postWage + postSupplies) * NEEDS_SCALING_FACTOR) : -1f);
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
            // the state's account at the bank (annex 13, decision 219): its share of the bank's losses and interest, like the
            // pops' deposits; it pays off the state's loans first (both claims cancel, reserves unchanged); a treasury below its
            // needs, target and commitments draws on it within the bank's reserves
            double stateDeposit = countryBank.stateDeposit() * (1.0 - countryBank.depositLossRatio());
            double depositInterest = Math.min(stateDeposit * countryBank.depositRate(), Math.max(0.0, countryBank.interestPool()));
            if (depositInterest > 0.0) {
                countryBank.interestPool(countryBank.interestPool() - depositInterest);
                countryMarket.treasury(countryMarket.treasury() + depositInterest);
            }
            double netted = Math.min(stateDeposit, stateLoans);
            if (netted > 0.0) {
                stateDeposit -= netted;
                stateLoans -= netted;
                countryBank.repaid(STATE_OWNER, countryBank.repaid(STATE_OWNER) + netted);
            }
            double treasuryFloor = needs * (1.0 + TARGET_TREASURY_DAYS) + countryInvestment.committed(STATE_OWNER);
            double withdrawal = Math.min(Math.max(0.0, treasuryFloor - countryMarket.treasury()), Math.min(stateDeposit, Math.max(0.0, countryBank.reserves())));
            if (withdrawal > 0.0) {
                stateDeposit -= withdrawal;
                countryBank.reserves(countryBank.reserves() - withdrawal);
                countryMarket.treasury(countryMarket.treasury() + withdrawal);
            }
            countryBank.stateDeposit(stateDeposit);
            countryBank.deposits(countryBank.deposits() + stateDeposit);
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
            // when the treasury cannot pay every need, the state posts take the cut first (decision 228): the army, aid,
            // pensions and upkeep are paid before the civil servants
            float spendingRatio = otherNeeds > treasury ? (float) (treasury / otherNeeds) : 1f;
            float postRatio = postNeeds > 0.0 ? (float) Math.clamp((treasury - otherNeeds) / postNeeds, 0.0, 1.0) : 1f;
            double targetTreasury = TARGET_TREASURY_DAYS * needs;
            double excess = treasury - needs - targetTreasury;

            float socialSlider = slider(countryBudgetPolicy.socialSpendingRate());
            double wagesBudget = 0.0;
            double socialBudget = 0.0;
            double pensionBudget = 0.0;
            for (int p = 0; p < POP_TYPE_COUNT; p++) {
                float popTypeWageRate = 0f;
                float popTypeSocialRate = 0f;
                if (assistedTypes[p]) {
                    popTypeSocialRate = spendingRatio * socialRate(countryMarket, p, socialSlider);
                    socialBudget += (double) scaledUnemployed(countryDemographics, p, EmploymentUtils.activeShare(baseWorkforceShares[p], workforceShareModifier)) * popTypeSocialRate;
                } else {
                    popTypeWageRate = (p == statePostIndex ? postRatio : spendingRatio) * wageRate(countryMarket, p, wageSlider(incomeTypes[p], countryBudgetPolicy, countryEducationPolicy));
                    wagesBudget += scaledWageBase(countryDemographics, p, statePostIndex, incomeTypes[p]) * popTypeWageRate;
                }
                countryBudget.wageRatesByPopType(p, popTypeWageRate);
                countryBudget.socialRatesByPopType(p, popTypeSocialRate);
                float popTypePensionRate = spendingRatio * pensionRate(countryMarket, p, countryBudgetPolicy.pensionRate());
                pensionBudget += (double) countryDemographics.seniorsByPopType(p) / NEEDS_SCALING_FACTOR * popTypePensionRate;
                countryBudget.pensionRatesByPopType(p, popTypePensionRate);
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
            // the state can also spend what it can draw from its account (decision 219)
            investableSurplus += Math.min(stateDeposit, Math.max(0.0, countryBank.reserves()));
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
            countryBudget.pensionBudget((float) pensionBudget);
            countryBudget.pensionCoverage(spendingRatio * countryBudgetPolicy.pensionRate());
            countryBudget.stateConsumptionBudget((float) (spendingRatio * healthConsumptionCost + postRatio * administrationConsumptionCost));
            countryBudget.militarySupplyRatio(spendingRatio);
        }
    }
}

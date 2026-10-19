package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

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
            .multiThreaded()
            .iter(this::budget);
    }

    private void budget(Iter iter) {
        int[] incomeTypes = new int[POP_TYPE_COUNT];
        boolean[] assistedTypes = new boolean[POP_TYPE_COUNT];
        readPopulationTypes(iter.world(), incomeTypes, assistedTypes);
        int statePostIndex = statePostIndex(iter.world());
        GlobalMarketView globalMarket = iter.world().obtainEntityView(iter.world().lookup("global_market")).getMutView(GlobalMarket.class);

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        Field<CountryDemographics> countryDemographicsField = iter.field(CountryDemographics.class, 1);
        Field<CountryBudgetPolicy> countryBudgetPolicyField = iter.field(CountryBudgetPolicy.class, 2);
        Field<CountryEducationPolicy> countryEducationPolicyField = iter.field(CountryEducationPolicy.class, 3);
        Field<CountryTradePolicy> countryTradePolicyField = iter.field(CountryTradePolicy.class, 4);
        Field<CountryBudget> countryBudgetField = iter.field(CountryBudget.class, 5);
        Field<CountryBank> countryBankField = iter.field(CountryBank.class, 6);
        Field<CountryInvestment> countryInvestmentField = iter.field(CountryInvestment.class, 7);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
            CountryDemographicsView countryDemographics = countryDemographicsField.getMutView(i);
            CountryBudgetPolicyView countryBudgetPolicy = countryBudgetPolicyField.getMutView(i);
            CountryEducationPolicyView countryEducationPolicy = countryEducationPolicyField.getMutView(i);
            CountryTradePolicyView countryTradePolicy = countryTradePolicyField.getMutView(i);
            CountryBudgetMutView countryBudget = countryBudgetField.getMutView(i);
            CountryBankMutView countryBank = countryBankField.getMutView(i);
            CountryInvestmentMutView countryInvestment = countryInvestmentField.getMutView(i);

            double subsidies = 0.0;
            float tariffRate = countryTradePolicy.tariffRate();
            if (tariffRate < 0f) {
                for (int g = 0; g < countryMarket.goodImportedAmountsLength(); g++) {
                    subsidies += (double) countryMarket.goodImportedAmounts(g) * globalMarket.goodPrices(g) * -tariffRate;
                }
            }

            double needs = dailyNeeds(incomeTypes, assistedTypes, countryMarket, countryDemographics, countryBudgetPolicy, countryEducationPolicy, statePostIndex) + subsidies;
            double stateLoans = countryBank.stateLoans() * (1.0 + STATE_LOAN_INTEREST_RATE / DAYS_PER_YEAR);
            double treasury = Math.max(0.0, countryMarket.treasury());
            double loan = needs > treasury ? Math.min(needs - treasury, Math.max(0.0, countryBank.deposits())) : 0.0;
            if (loan > 0.0) {
                countryBank.deposits(countryBank.deposits() - loan);
                countryMarket.treasury(countryMarket.treasury() + loan);
                stateLoans += loan;
                treasury += loan;
            }
            float spendingRatio = needs > treasury ? (float) (treasury / needs) : 1f;
            double targetTreasury = TARGET_TREASURY_DAYS * needs;
            double excess = treasury - needs - targetTreasury;
            if (excess > 0.0 && stateLoans > 0.0) {
                double repayment = Math.min(stateLoans, excess);
                countryMarket.treasury(countryMarket.treasury() - repayment);
                countryBank.deposits(countryBank.deposits() + repayment);
                stateLoans -= repayment;
                excess -= repayment;
            }
            countryBank.stateLoans(stateLoans);
            countryBank.loansToday((float) loan);

            float socialSlider = slider(countryBudgetPolicy.socialSpendingRate());
            double wagesBudget = 0.0;
            double socialBudget = 0.0;
            double assistedUnits = 0.0;
            for (int p = 0; p < POP_TYPE_COUNT; p++) {
                float popTypeWageRate = 0f;
                float popTypeSocialRate = 0f;
                if (assistedTypes[p]) {
                    popTypeSocialRate = spendingRatio * socialRate(countryMarket, p, socialSlider);
                    socialBudget += (double) scaledUnemployed(countryDemographics, p) * popTypeSocialRate;
                    assistedUnits += countryDemographics.totalByPopType(p) / NEEDS_SCALING_FACTOR;
                } else {
                    popTypeWageRate = spendingRatio * wageRate(countryMarket, p, wageSlider(incomeTypes[p], countryBudgetPolicy, countryEducationPolicy));
                    wagesBudget += scaledWageBase(countryDemographics, p, statePostIndex) * popTypeWageRate;
                }
                countryBudget.wageRatesByPopType(p, popTypeWageRate);
                countryBudget.socialRatesByPopType(p, popTypeSocialRate);
            }

            double investableSurplus = Math.max(0.0, excess - countryInvestment.committed(STATE_OWNER));
            double transfers = assistedUnits > 0.0 ? EXCESS_TREASURY_SPENDING_RATE * investableSurplus : 0.0;
            for (int p = 0; p < POP_TYPE_COUNT; p++) {
                countryBudget.transferRatesByPopType(p, assistedTypes[p] && transfers > 0.0 ? (float) (transfers / assistedUnits) : 0f);
            }

            countryBudget.targetTreasury(targetTreasury);
            countryBudget.reservedNeeds(needs);
            countryBudget.investableSurplus(investableSurplus);
            countryBudget.spendingRatio(spendingRatio);
            countryBudget.subsidyAllowance((float) (spendingRatio * subsidies));
            countryBudget.wagesBudget((float) wagesBudget);
            countryBudget.socialBudget((float) socialBudget);
            countryBudget.transfersBudget((float) transfers);
        }
    }
}

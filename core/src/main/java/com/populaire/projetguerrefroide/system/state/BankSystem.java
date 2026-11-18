package com.populaire.projetguerrefroide.system.state;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;

import static com.populaire.projetguerrefroide.simulation.Constants.*;

public class BankSystem {
    private final EcsConstants ecsConstants;

    public BankSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("BankSystem")
            .kind(phaseId)
            .with(CountryBank.class)
            .with(CountryInvestment.class)
            .with(CountryCentralBank.class)
            .with(CountryMarket.class)
            .iter(this::settle);
    }

    private void settle(Iter iter) {
        int day = iter.stage().obtainEntityView(this.ecsConstants.economyCalendar()).<EconomyCalendarView>getView(EconomyCalendar.class).day();
        double trendWeight = Math.min(DAYS_PER_YEAR, day + 1.0);
        Field<CountryBank> countryBankField = iter.field(CountryBank.class, 0);
        Field<CountryInvestment> countryInvestmentField = iter.field(CountryInvestment.class, 1);
        Field<CountryCentralBank> countryCentralBankField = iter.field(CountryCentralBank.class, 2);
        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 3);
        for (int i = 0; i < iter.count(); i++) {
            CountryBankMutView countryBank = countryBankField.getMutView(i);
            CountryInvestmentMutView countryInvestment = countryInvestmentField.getMutView(i);
            CountryCentralBankMutView countryCentralBank = countryCentralBankField.getMutView(i);
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);

            for (int o = 0; o < OWNER_COUNT; o++) {
                if (o == STATE_OWNER) {
                    continue;
                }
                double loans = countryBank.loans(o);
                if (loans <= 0.0) {
                    continue;
                }
                double interest = loans * countryBank.lendingRate() / DAYS_PER_YEAR;
                double funds = Math.max(0.0, countryInvestment.funds(o));
                if (funds < interest) {
                    countryInvestment.funds(o, countryInvestment.funds(o) - funds);
                    countryBank.interestPool(countryBank.interestPool() + funds);
                    countryBank.interestPaid(o, countryBank.interestPaid(o) + funds);
                    countryBank.losses(countryBank.losses() + loans);
                    countryBank.writtenOff(o, countryBank.writtenOff(o) + loans);
                    countryBank.bankruptcies(countryBank.bankruptcies() + 1);
                    countryBank.loans(o, 0.0);
                    continue;
                }
                double principalPaid = Math.min(loans / BANK_LOAN_TERM_DAYS, funds - interest);
                countryInvestment.funds(o, countryInvestment.funds(o) - interest - principalPaid);
                countryBank.reserves(countryBank.reserves() + principalPaid);
                countryBank.interestPool(countryBank.interestPool() + interest);
                countryBank.interestPaid(o, countryBank.interestPaid(o) + interest);
                countryBank.repaid(o, countryBank.repaid(o) + principalPaid);
                countryBank.loans(o, loans - principalPaid);
            }

            double advances = countryBank.centralBankAdvances();
            double advanceInterest = advances * countryCentralBank.policyRate() / DAYS_PER_YEAR;
            if (advanceInterest > 0.0) {
                double fromPool = Math.min(advanceInterest, Math.max(0.0, countryBank.interestPool()));
                double fromReserves = Math.min(advanceInterest - fromPool, Math.max(0.0, countryBank.reserves()));
                countryBank.interestPool(countryBank.interestPool() - fromPool);
                countryBank.reserves(countryBank.reserves() - fromReserves);
                countryMarket.treasury(countryMarket.treasury() + fromPool + fromReserves);
                countryCentralBank.remittances(countryCentralBank.remittances() + fromPool + fromReserves);
            }

            double deposits = countryBank.deposits();
            countryBank.depositRate(deposits > 0.0 ? countryBank.interestPool() / deposits : 0.0);
            countryBank.depositLossRatio(deposits > 0.0 ? (float) Math.min(1.0, countryBank.losses() / deposits) : 0f);
            countryBank.losses(0.0);

            double repaid = Math.min(advances, Math.max(0.0, countryBank.reserves()));
            if (repaid > 0.0) {
                countryBank.reserves(countryBank.reserves() - repaid);
                advances -= repaid;
                countryBank.centralBankAdvances(advances);
            }

            float lendingRate = countryCentralBank.policyRate();
            countryBank.lendingRate(lendingRate);
            for (int o = 0; o < OWNER_COUNT; o++) {
                double owed = o == STATE_OWNER ? 0.0 : countryBank.loans(o);
                countryBank.debtServiceDue(o, owed * lendingRate / DAYS_PER_YEAR + owed / BANK_LOAN_TERM_DAYS);
                if (o != STATE_OWNER) {
                    double income = countryBank.ownerIncome(o);
                    double trend = countryBank.ownerIncomeTrend(o);
                    countryBank.ownerIncomeTrend(o, trend + (income - trend) / trendWeight);
                }
            }
        }
    }
}

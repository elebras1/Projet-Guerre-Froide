package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.Constants.*;

public class BankSystem {

    public BankSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("BankSystem")
            .kind(phaseId)
            .with(CountryBank.class)
            .with(CountryInvestment.class)
            .iter(this::settle);
    }

    private void settle(Iter iter) {
        Field<CountryBank> countryBankField = iter.field(CountryBank.class, 0);
        Field<CountryInvestment> countryInvestmentField = iter.field(CountryInvestment.class, 1);
        for (int i = 0; i < iter.count(); i++) {
            CountryBankMutView countryBank = countryBankField.getMutView(i);
            CountryInvestmentMutView countryInvestment = countryInvestmentField.getMutView(i);

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

            double deposits = countryBank.deposits();
            countryBank.depositRate(deposits > 0.0 ? (float) (countryBank.interestPool() / deposits) : 0f);
            countryBank.depositLossRatio(deposits > 0.0 ? (float) Math.min(1.0, countryBank.losses() / deposits) : 0f);
            countryBank.losses(0.0);

            double loans = 0.0;
            for (int o = 0; o < OWNER_COUNT; o++) {
                loans += countryBank.loans(o);
            }
            double assets = loans + Math.max(0.0, countryBank.reserves());
            countryBank.lendingRate(assets > 0.0 ? (float) (MAXIMUM_LENDING_RATE * loans / assets) : MAXIMUM_LENDING_RATE);
            for (int o = 0; o < OWNER_COUNT; o++) {
                double owed = o == STATE_OWNER ? 0.0 : countryBank.loans(o);
                countryBank.debtServiceDue(o, owed * countryBank.lendingRate() / DAYS_PER_YEAR + owed / BANK_LOAN_TERM_DAYS);
            }
        }
    }
}

package com.populaire.projetguerrefroide.system.state;

import com.populaire.projetguerrefroide.component.CountryBankMutView;
import com.populaire.projetguerrefroide.component.CountryBankView;
import com.populaire.projetguerrefroide.component.CountryInvestmentMutView;

import static com.populaire.projetguerrefroide.simulation.Constants.CREDIT_LIMIT_YEARS;
import static com.populaire.projetguerrefroide.simulation.Constants.DAYS_PER_YEAR;

public class BankUtils {

    /**
     * Debt service withheld from an owner's share of the profits before it reaches its pops or its fund; the share itself
     * is the owner's income, followed over a year for its credit limit (decision 238).
     */
    public static float withholdDebtService(CountryBankMutView countryBank, CountryInvestmentMutView countryInvestment, int owner, float share) {
        if (share > 0f) {
            countryBank.ownerIncome(owner, countryBank.ownerIncome(owner) + share);
        }
        double due = countryBank.debtServiceDue(owner);
        if (due <= 0.0 || share <= 0f) {
            return share;
        }
        float withheld = (float) Math.min(share, due);
        countryBank.debtServiceDue(owner, due - withheld);
        countryInvestment.funds(owner, countryInvestment.funds(owner) + withheld);
        return share - withheld;
    }

    /**
     * A payment of the bank (a loan granted, a deposit withdrawn) out of its reserves (decision 235): what the reserves
     * lack is advanced by the central bank, the only point where money is created; {@link BankSystem} repays the advances
     * from the reserves every day.
     */
    public static void draw(CountryBankMutView countryBank, double amount) {
        double lacking = amount - countryBank.reserves();
        if (lacking > 0.0) {
            countryBank.centralBankAdvances(countryBank.centralBankAdvances() + lacking);
            countryBank.reserves(countryBank.reserves() + lacking);
        }
        countryBank.reserves(countryBank.reserves() - amount);
    }

    /** What a private owner may still borrow (decision 238): its credit limit, years of its income, less its loans. */
    public static double privateCreditHeadroom(CountryBankView countryBank, int owner) {
        return Math.max(0.0, CREDIT_LIMIT_YEARS * DAYS_PER_YEAR * countryBank.ownerIncomeTrend(owner) - countryBank.loans(owner));
    }
}

package com.populaire.projetguerrefroide.system.state;

import com.populaire.projetguerrefroide.component.CountryBankMutView;
import com.populaire.projetguerrefroide.component.CountryBankView;
import com.populaire.projetguerrefroide.component.CountryInvestmentMutView;

import static com.populaire.projetguerrefroide.simulation.Constants.CREDIT_LIMIT_YEARS;
import static com.populaire.projetguerrefroide.simulation.Constants.DAYS_PER_YEAR;

public class BankUtils {

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

    public static void draw(CountryBankMutView countryBank, double amount) {
        double lacking = amount - countryBank.reserves();
        if (lacking > 0.0) {
            countryBank.centralBankAdvances(countryBank.centralBankAdvances() + lacking);
            countryBank.reserves(countryBank.reserves() + lacking);
        }
        countryBank.reserves(countryBank.reserves() - amount);
    }

    public static double privateCreditHeadroom(CountryBankView countryBank, int owner) {
        return Math.max(0.0, CREDIT_LIMIT_YEARS * DAYS_PER_YEAR * countryBank.ownerIncomeTrend(owner) - countryBank.loans(owner));
    }
}

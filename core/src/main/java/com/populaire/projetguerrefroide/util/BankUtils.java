package com.populaire.projetguerrefroide.util;

import com.populaire.projetguerrefroide.component.CountryBankMutView;
import com.populaire.projetguerrefroide.component.CountryInvestmentMutView;

public class BankUtils {

    public static float withholdDebtService(CountryBankMutView countryBank, CountryInvestmentMutView countryInvestment, int owner, float share) {
        double due = countryBank.debtServiceDue(owner);
        if (due <= 0.0 || share <= 0f) {
            return share;
        }
        float withheld = (float) Math.min(share, due);
        countryBank.debtServiceDue(owner, due - withheld);
        countryInvestment.funds(owner, countryInvestment.funds(owner) + withheld);
        return share - withheld;
    }
}

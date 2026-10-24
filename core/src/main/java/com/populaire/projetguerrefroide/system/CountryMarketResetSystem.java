package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.CountryBank;
import com.populaire.projetguerrefroide.component.CountryBankMutView;
import com.populaire.projetguerrefroide.component.CountryMarket;
import com.populaire.projetguerrefroide.component.CountryMarketMutView;

public class CountryMarketResetSystem {

    public CountryMarketResetSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("CountryMarketResetSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .with(CountryBank.class)
            .multiThreaded()
            .iter(this::reset);
    }

    private void reset(Iter iter) {
        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        Field<CountryBank> countryBankField = iter.field(CountryBank.class, 1);
        for(int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
            CountryBankMutView countryBank = countryBankField.getMutView(i);

            for(int g = 0; g < countryMarket.goodDemandAmountsLength(); g++) {
                countryMarket.goodDemandAmounts(g, 0f);
                countryMarket.goodPopulationDemandAmounts(g, 0f);
                countryMarket.goodProducedAmounts(g, 0f);
                countryMarket.goodConsumedAmounts(g, 0f);
                countryMarket.goodImportNeeds(g, 0f);
                countryMarket.goodExportOffers(g, 0f);
                countryMarket.goodImportedAmounts(g, 0f);
                countryMarket.goodExportedAmounts(g, 0f);
                countryMarket.goodLostAmounts(g, 0f);
            }
            countryMarket.tariffRevenue(0f);
            countryMarket.taxRevenue(0.0);
            countryMarket.enterpriseSupport(0.0);
            countryMarket.profitRevenue(0.0);
            countryBank.deposits(0.0);

        }
    }
}

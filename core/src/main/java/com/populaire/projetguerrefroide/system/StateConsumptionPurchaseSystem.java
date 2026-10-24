package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class StateConsumptionPurchaseSystem {

    public StateConsumptionPurchaseSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("StateConsumptionPurchaseSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .with(CountryStateConsumption.class)
            .iter(this::purchase);
    }

    private void purchase(Iter iter) {
        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        Field<CountryStateConsumption> countryStateConsumptionField = iter.field(CountryStateConsumption.class, 1);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
            CountryStateConsumptionMutView countryStateConsumption = countryStateConsumptionField.getMutView(i);

            double paid = 0.0;
            for (int g = 0; g < countryStateConsumption.demandedAmountsLength(); g++) {
                float demanded = countryStateConsumption.demandedAmounts(g);
                float demand = countryMarket.goodDemandAmounts(g);
                if (demanded <= 0f || demand <= 0f) {
                    continue;
                }
                float delivered = demanded * countryMarket.goodConsumedAmounts(g) / demand;
                double cost = (double) delivered * countryMarket.goodPrices(g);
                countryMarket.goodRevenues(g, countryMarket.goodRevenues(g) + cost);
                paid += cost;
            }
            countryMarket.treasury(countryMarket.treasury() - paid);
            countryStateConsumption.spentToday(paid);
        }
    }
}

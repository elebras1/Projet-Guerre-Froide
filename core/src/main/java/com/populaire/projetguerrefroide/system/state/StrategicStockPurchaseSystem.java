package com.populaire.projetguerrefroide.system.state;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class StrategicStockPurchaseSystem {

    public StrategicStockPurchaseSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("StrategicStockPurchaseSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .with(CountryStrategicStock.class)
            .iter(this::purchase);
    }

    private void purchase(Iter iter) {
        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        Field<CountryStrategicStock> countryStrategicStockField = iter.field(CountryStrategicStock.class, 1);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
            CountryStrategicStockMutView stock = countryStrategicStockField.getMutView(i);
            double paid = 0.0;
            for (int g = 0; g < stock.demandedAmountsLength(); g++) {
                float demanded = stock.demandedAmounts(g);
                float demand = countryMarket.goodDemandAmounts(g);
                if (demanded <= 0f || demand <= 0f) {
                    stock.purchasedAmounts(g, 0f);
                    continue;
                }
                float delivered = demanded * countryMarket.goodConsumedAmounts(g) / demand;
                double cost = (double) delivered * countryMarket.goodPrices(g);
                countryMarket.goodRevenues(g, countryMarket.goodRevenues(g) + cost);
                stock.stocks(g, stock.stocks(g) + delivered);
                stock.purchasedAmounts(g, delivered);
                paid += cost;
            }
            countryMarket.treasury(countryMarket.treasury() - paid);
            stock.spentToday(paid);
        }
    }
}

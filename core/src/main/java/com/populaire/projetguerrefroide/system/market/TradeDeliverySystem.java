package com.populaire.projetguerrefroide.system.market;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;

import static com.populaire.projetguerrefroide.simulation.Constants.GOOD_COUNT;

public class TradeDeliverySystem {
    private final EcsConstants ecsConstants;

    public TradeDeliverySystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("TradeDeliverySystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .run(this::deliver);
    }

    private void deliver(Iter iter) {
        World world = iter.stage();
        boolean[] localGoods = this.ecsConstants.localGoods();
        GlobalMarketMutView globalMarket = world.obtainEntityView(this.ecsConstants.globalMarket()).getMutView(GlobalMarket.class);
        for (int g = 0; g < GOOD_COUNT; g++) {
            globalMarket.goodDeliveredWeights(g, 0.0);
            globalMarket.goodDeliveredValues(g, 0.0);
        }
        while (iter.next()) {
            Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
                for (int g = 0; g < GOOD_COUNT; g++) {
                    float demand = countryMarket.goodDemandAmounts(g);
                    float stock = countryMarket.goodStocks(g);
                    double sales = (double) demand * (1f - countryMarket.goodImportShares(g));
                    double ownWeight = countryMarket.goodTradeWeights(g);
                    if (!localGoods[g] && ownWeight > 0.0) {
                        sales += ownWeight * Math.max(0.0, globalMarket.goodForeignDemands(g) - ownForeignDemand(globalMarket, countryMarket, g));
                    }
                    float delivery = sales > 0.0 ? (float) Math.min(1.0, stock / sales) : 1f;
                    countryMarket.goodSalesAmounts(g, (float) sales);
                    countryMarket.goodDeliveryRatios(g, delivery);
                    if (!localGoods[g] && ownWeight > 0.0) {
                        globalMarket.goodDeliveredWeights(g, globalMarket.goodDeliveredWeights(g) + ownWeight * delivery);
                        globalMarket.goodDeliveredValues(g, globalMarket.goodDeliveredValues(g) + ownWeight * delivery * countryMarket.goodProducerPrices(g));
                    }
                }
            }
        }
    }

    static double ownForeignDemand(GlobalMarketView globalMarket, CountryMarketView countryMarket, int goodIndex) {
        float foreignDemand = countryMarket.goodImportNeeds(goodIndex);
        double foreignWeight = globalMarket.goodTradeWeights(goodIndex) - countryMarket.goodTradeWeights(goodIndex);
        return foreignDemand > 0f && foreignWeight > 0.0 ? foreignDemand / foreignWeight : 0.0;
    }
}

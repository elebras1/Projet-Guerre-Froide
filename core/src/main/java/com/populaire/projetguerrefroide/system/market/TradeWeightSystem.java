package com.populaire.projetguerrefroide.system.market;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;

import static com.populaire.projetguerrefroide.simulation.Constants.GOOD_COUNT;

public class TradeWeightSystem {
    private final EcsConstants ecsConstants;

    public TradeWeightSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("TradeWeightSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .run(this::weigh);
    }

    private void weigh(Iter iter) {
        World world = iter.stage();
        boolean[] localGoods = this.ecsConstants.localGoods();
        float[] costs = this.ecsConstants.goodCosts();
        GlobalMarketMutView globalMarket = world.obtainEntityView(this.ecsConstants.globalMarket()).getMutView(GlobalMarket.class);
        for (int g = 0; g < GOOD_COUNT; g++) {
            globalMarket.goodTradeWeights(g, 0.0);
            globalMarket.goodTradeWeightedPrices(g, 0.0);
        }
        while (iter.next()) {
            Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
                for (int g = 0; g < GOOD_COUNT; g++) {
                    if (localGoods[g]) {
                        countryMarket.goodTradeWeights(g, 0.0);
                        continue;
                    }
                    float price = countryMarket.goodProducerPrices(g);
                    double weight = MarketUtils.tradeWeight(countryMarket.goodStocks(g), price, costs[g]);
                    countryMarket.goodTradeWeights(g, weight);
                    if (weight > 0.0) {
                        globalMarket.goodTradeWeights(g, globalMarket.goodTradeWeights(g) + weight);
                        globalMarket.goodTradeWeightedPrices(g, globalMarket.goodTradeWeightedPrices(g) + weight * price);
                    }
                }
            }
        }
    }
}

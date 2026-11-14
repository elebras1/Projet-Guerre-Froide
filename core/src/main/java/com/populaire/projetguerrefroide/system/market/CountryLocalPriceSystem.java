package com.populaire.projetguerrefroide.system.market;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;

import static com.populaire.projetguerrefroide.simulation.Constants.*;

/**
 * Price of the goods that are not traded between countries (decision 207): each country moves its own price with the gap
 * between its demand and its production, by the rule and within the bounds of the world price
 * ({@link GlobalMarketResolveSystem}); the world price of such a good is the average of the country prices weighted by the
 * demand, for the statistics and the world-level readers.
 */
public class CountryLocalPriceSystem {
    private final EcsConstants ecsConstants;
    private final double[] weightedPrices = new double[GOOD_COUNT];
    private final double[] demands = new double[GOOD_COUNT];

    public CountryLocalPriceSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("CountryLocalPriceSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .run(this::price);
    }

    private void price(Iter iter) {
        World world = iter.stage();
        boolean[] localGoods = this.ecsConstants.localGoods();
        float[] costs = this.ecsConstants.goodCosts();
        GlobalGoodView globalGood = world.obtainEntityView(this.ecsConstants.globalGood()).getView(GlobalGood.class);
        int serviceStart = globalGood.serviceGoodStartIndex();
        int serviceEnd = globalGood.serviceGoodEndIndex();
        double[] weightedPrices = this.weightedPrices;
        double[] demands = this.demands;
        java.util.Arrays.fill(weightedPrices, 0.0);
        java.util.Arrays.fill(demands, 0.0);
        while (iter.next()) {
            Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
                for (int g = 0; g < GOOD_COUNT; g++) {
                    if (!localGoods[g]) {
                        continue;
                    }
                    float demand = countryMarket.goodDemandAmounts(g);
                    float production = countryMarket.goodProducedAmounts(g);
                    float offer = production;
                    if (g < serviceStart || g >= serviceEnd) {
                        float inventory = countryMarket.goodStocks(g) - production;
                        offer += (inventory - STOCK_TARGET_DAYS * demand) / STOCK_ADJUSTMENT_DAYS;
                    }
                    float scale = Math.max(demand, offer);
                    float excessDemand = scale > 0f ? Math.clamp((demand - offer) / scale, -1f, 1f) : 0f;
                    float price = countryMarket.goodPrices(g) * (1f + PRICE_ADJUSTMENT_RATE * excessDemand);
                    price = Math.clamp(price, MINIMUM_PRICE_RATIO * costs[g], MAXIMUM_PRICE_RATIO * costs[g]);
                    countryMarket.goodPrices(g, price);
                    weightedPrices[g] += (double) price * demand;
                    demands[g] += demand;
                }
            }
        }
        GlobalMarketMutView globalMarket = world.obtainEntityView(this.ecsConstants.globalMarket()).getMutView(GlobalMarket.class);
        for (int g = 0; g < GOOD_COUNT; g++) {
            if (localGoods[g] && demands[g] > 0.0) {
                globalMarket.goodPrices(g, (float) (weightedPrices[g] / demands[g]));
            }
        }
    }
}

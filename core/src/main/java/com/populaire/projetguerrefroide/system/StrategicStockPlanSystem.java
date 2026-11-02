package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.Constants.*;

public class StrategicStockPlanSystem {

    public StrategicStockPlanSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("StrategicStockPlanSystem")
            .kind(phaseId)
            .with(CountryStrategicStock.class)
            .with(CountryMarket.class)
            .with(CountryBudget.class)
            .iter(this::plan);
    }

    private void plan(Iter iter) {
        World world = iter.world();
        EntityView globalStorage = world.obtainEntityView(world.lookup("global_strategic_storage"));
        if (!globalStorage.has(GlobalStrategicStorage.class)) {
            return;
        }
        GlobalStrategicStorageView strategicStorage = globalStorage.getView(GlobalStrategicStorage.class);
        GlobalGoodView globalGood = world.obtainEntityView(world.lookup("global_good")).getView(GlobalGood.class);
        int goodCount = globalGood.goodIdsLength();
        float[] costs = new float[goodCount];
        float[] decays = new float[goodCount];
        for (int g = 0; g < goodCount; g++) {
            GoodView good = world.obtainEntityView(globalGood.goodIds(g)).getView(Good.class);
            costs[g] = good.cost();
            decays[g] = good.stockDecay() * STORAGE_DECAY_FACTOR;
        }
        float[] weights = new float[goodCount];

        Field<CountryStrategicStock> countryStrategicStockField = iter.field(CountryStrategicStock.class, 0);
        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 1);
        Field<CountryBudget> countryBudgetField = iter.field(CountryBudget.class, 2);
        for (int i = 0; i < iter.count(); i++) {
            CountryStrategicStockMutView stock = countryStrategicStockField.getMutView(i);
            CountryMarketView countryMarket = countryMarketField.getMutView(i);
            CountryBudgetMutView countryBudget = countryBudgetField.getMutView(i);

            double stockValue = 0.0;
            for (int g = 0; g < goodCount; g++) {
                stock.demandedAmounts(g, 0f);
                stock.plannedReleases(g, 0f);
                float amount = stock.stocks(g);
                float lost = amount * decays[g];
                stock.lostAmounts(g, lost);
                if (amount > 0f) {
                    stock.stocks(g, amount - lost);
                }
                stockValue += (double) stock.stocks(g) * costs[g];
            }
            if (stockValue <= 0.0 && stock.capacity() <= 0.0) {
                continue;
            }

            double capacity = stock.capacity();
            double excessShare = stockValue > capacity && stockValue > 0.0 ? (stockValue - capacity) / stockValue : 0.0;
            double releasedValue = 0.0;
            for (int g = 0; g < goodCount; g++) {
                float amount = stock.stocks(g);
                if (amount <= 0f || costs[g] <= 0f) {
                    continue;
                }
                float release = (float) (amount * excessShare);
                float priceRatio = countryMarket.goodPrices(g) / costs[g];
                boolean shortage = priceRatio >= 1f && countryMarket.goodStocks(g) < STOCK_TARGET_DAYS * countryMarket.goodDemandAmounts(g);
                if (priceRatio > STORAGE_SELL_PRICE_RATIO || shortage) {
                    release = Math.max(release, amount / STORAGE_RELEASE_DAYS);
                }
                if (release > 0f) {
                    stock.plannedReleases(g, release);
                    releasedValue += (double) release * costs[g];
                }
            }

            double freeCapacity = capacity - (stockValue - releasedValue);
            double budget = STORAGE_BUY_RATE * countryBudget.investableSurplus();
            if (freeCapacity <= 0.0 || budget <= 0.0) {
                continue;
            }
            double totalWeight = 0.0;
            for (int g = 0; g < goodCount; g++) {
                weights[g] = 0f;
                if (!strategicStorage.storedGoods(g) || costs[g] <= 0f || stock.plannedReleases(g) > 0f) {
                    continue;
                }
                if (countryMarket.goodPrices(g) / costs[g] < STORAGE_BUY_PRICE_RATIO) {
                    weights[g] = Math.max(0f, countryMarket.goodConsumedAmounts(g) - stock.purchasedAmounts(g)) * costs[g];
                    totalWeight += weights[g];
                }
            }
            if (totalWeight <= 0.0) {
                continue;
            }
            double meanPriceRatio = 0.0;
            for (int g = 0; g < goodCount; g++) {
                if (weights[g] > 0f) {
                    meanPriceRatio += weights[g] / totalWeight * countryMarket.goodPrices(g) / costs[g];
                }
            }
            double baseValue = meanPriceRatio > 0.0 ? Math.min(freeCapacity, budget / meanPriceRatio) : freeCapacity;
            for (int g = 0; g < goodCount; g++) {
                if (weights[g] > 0f) {
                    stock.demandedAmounts(g, (float) (baseValue * weights[g] / totalWeight / costs[g]));
                }
            }
            countryBudget.investableSurplus(Math.max(0.0, countryBudget.investableSurplus() - baseValue * meanPriceRatio));
        }
    }
}

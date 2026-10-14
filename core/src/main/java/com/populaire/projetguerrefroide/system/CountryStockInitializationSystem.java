package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.Constants.STOCK_TARGET_DAYS;

public class CountryStockInitializationSystem {

    public CountryStockInitializationSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("CountryStockInitializationSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .multiThreaded()
            .iter(this::initialize);
    }

    private void initialize(Iter iter) {
        EntityView globalGood = iter.world().obtainEntityView(iter.world().lookup("global_good"));
        GlobalGoodView globalGoodData = globalGood.getMutView(GlobalGood.class);
        int serviceGoodStartIndex = globalGoodData.serviceGoodStartIndex();
        int serviceGoodEndIndex = globalGoodData.serviceGoodEndIndex();

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);

            for (int g = 0; g < countryMarket.goodStocksLength(); g++) {
                if (g < serviceGoodStartIndex || g >= serviceGoodEndIndex) {
                    countryMarket.goodStocks(g, countryMarket.goodStocks(g) + STOCK_TARGET_DAYS * countryMarket.goodDemandAmounts(g));
                }
            }
        }
    }
}

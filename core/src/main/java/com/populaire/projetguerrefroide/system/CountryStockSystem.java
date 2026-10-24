package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.CountryMarket;
import com.populaire.projetguerrefroide.component.CountryMarketMutView;
import com.populaire.projetguerrefroide.component.GlobalGood;
import com.populaire.projetguerrefroide.component.GlobalGoodView;
import com.populaire.projetguerrefroide.component.Good;
import com.populaire.projetguerrefroide.component.GoodView;

public class CountryStockSystem {

    public CountryStockSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("CountryStockSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .multiThreaded()
            .iter(this::store);
    }

    private void store(Iter iter) {
        EntityView globalGood = iter.world().obtainEntityView(iter.world().lookup("global_good"));
        GlobalGoodView globalGoodData = globalGood.getMutView(GlobalGood.class);
        int serviceGoodStartIndex = globalGoodData.serviceGoodStartIndex();
        int serviceGoodEndIndex = globalGoodData.serviceGoodEndIndex();
        float[] stockDecays = new float[globalGoodData.goodIdsLength()];
        for (int g = 0; g < stockDecays.length; g++) {
            GoodView good = iter.world().obtainEntityView(globalGoodData.goodIds(g)).getView(Good.class);
            stockDecays[g] = good.stockDecay();
        }

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        for(int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);

            for(int g = 0; g < countryMarket.goodStocksLength(); g++) {
                float lost = g >= serviceGoodStartIndex && g < serviceGoodEndIndex ? countryMarket.goodStocks(g) : countryMarket.goodStocks(g) * stockDecays[g];
                countryMarket.goodLostAmounts(g, lost);
                countryMarket.goodStocks(g, countryMarket.goodStocks(g) - lost);
            }

            for(int g = 0; g < countryMarket.goodStocksLength(); g++) {
                countryMarket.goodStocks(g, countryMarket.goodStocks(g) + countryMarket.goodProducedAmounts(g));
            }
        }
    }
}

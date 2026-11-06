package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;

import static com.populaire.projetguerrefroide.util.Constants.STOCK_TARGET_DAYS;

public class CountryStockInitializationSystem {
    private final EcsConstants ecsConstants;

    public CountryStockInitializationSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("CountryStockInitializationSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .multiThreaded()
            .iter(this::initialize);
    }

    private void initialize(Iter iter) {
        EntityView globalGood = iter.world().obtainEntityView(this.ecsConstants.globalGood());
        GlobalGoodView globalGoodData = globalGood.getMutView(GlobalGood.class);
        int serviceGoodStartIndex = globalGoodData.serviceGoodStartIndex();
        int serviceGoodEndIndex = globalGoodData.serviceGoodEndIndex();
        EntityView globalMarket = iter.world().obtainEntityView(this.ecsConstants.globalMarket());
        GlobalMarketView globalMarketData = globalMarket.getMutView(GlobalMarket.class);

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);

            for (int g = 0; g < countryMarket.goodStocksLength(); g++) {
                if ((g < serviceGoodStartIndex || g >= serviceGoodEndIndex) && globalMarketData.goodAvailabilities(g)) {
                    countryMarket.goodStocks(g, STOCK_TARGET_DAYS * countryMarket.goodDemandAmounts(g));
                }
            }
        }
    }
}

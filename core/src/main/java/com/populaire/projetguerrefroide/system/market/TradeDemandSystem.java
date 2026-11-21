package com.populaire.projetguerrefroide.system.market;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;

import static com.populaire.projetguerrefroide.simulation.Constants.GOOD_COUNT;

public class TradeDemandSystem {
    private final EcsConstants ecsConstants;

    public TradeDemandSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("TradeDemandSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .run(this::demand);
    }

    private void demand(Iter iter) {
        World world = iter.stage();
        boolean[] localGoods = this.ecsConstants.localGoods();
        GlobalMarketMutView globalMarket = world.obtainEntityView(this.ecsConstants.globalMarket()).getMutView(GlobalMarket.class);
        for (int g = 0; g < GOOD_COUNT; g++) {
            globalMarket.goodForeignDemands(g, 0.0);
        }
        while (iter.next()) {
            Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
                for (int g = 0; g < GOOD_COUNT; g++) {
                    float importShare = countryMarket.goodImportShares(g);
                    if (localGoods[g] || importShare <= 0f) {
                        countryMarket.goodImportNeeds(g, 0f);
                        continue;
                    }
                    float foreignDemand = countryMarket.goodDemandAmounts(g) * importShare;
                    countryMarket.goodImportNeeds(g, foreignDemand);
                    double foreignWeight = globalMarket.goodTradeWeights(g) - countryMarket.goodTradeWeights(g);
                    if (foreignDemand > 0f && foreignWeight > 0.0) {
                        globalMarket.goodForeignDemands(g, globalMarket.goodForeignDemands(g) + foreignDemand / foreignWeight);
                    }
                }
            }
        }
    }
}

package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class StrategicStockReleaseSystem {

    public StrategicStockReleaseSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("StrategicStockReleaseSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .with(CountryStrategicStock.class)
            .iter(this::release);
    }

    private void release(Iter iter) {
        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        Field<CountryStrategicStock> countryStrategicStockField = iter.field(CountryStrategicStock.class, 1);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
            CountryStrategicStockMutView stock = countryStrategicStockField.getMutView(i);
            for (int g = 0; g < stock.plannedReleasesLength(); g++) {
                float released = Math.min(stock.plannedReleases(g), stock.stocks(g));
                stock.releasedAmounts(g, Math.max(0f, released));
                stock.plannedReleases(g, 0f);
                if (released <= 0f) {
                    continue;
                }
                stock.stocks(g, stock.stocks(g) - released);
                countryMarket.goodProducedAmounts(g, countryMarket.goodProducedAmounts(g) + released);
            }
        }
    }
}

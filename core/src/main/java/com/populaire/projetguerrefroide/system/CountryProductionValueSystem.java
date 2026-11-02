package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.CountryMarket;
import com.populaire.projetguerrefroide.component.CountryMarketMutView;
import com.populaire.projetguerrefroide.component.CountryStrategicStock;
import com.populaire.projetguerrefroide.component.CountryStrategicStockMutView;
import com.populaire.projetguerrefroide.component.CountryStrategicStockView;

public class CountryProductionValueSystem {

    public CountryProductionValueSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("CountryProductionValueSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .with(CountryStrategicStock.class)
            .multiThreaded()
            .iter(this::compute);
    }

    private void compute(Iter iter) {
        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        Field<CountryStrategicStock> countryStrategicStockField = iter.field(CountryStrategicStock.class, 1);
        for(int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
            CountryStrategicStockView stock = countryStrategicStockField.getMutView(i);

            float productionValue = 0f;
            for(int g = 0; g < countryMarket.goodProducedAmountsLength(); g++) {
                productionValue += (countryMarket.goodProducedAmounts(g) - stock.releasedAmounts(g)) * countryMarket.goodPrices(g);
            }
            countryMarket.productionValue(productionValue);
        }
    }
}

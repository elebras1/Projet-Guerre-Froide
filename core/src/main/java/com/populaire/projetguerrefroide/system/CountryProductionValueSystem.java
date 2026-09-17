package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.CountryMarket;
import com.populaire.projetguerrefroide.component.CountryMarketView;

public class CountryProductionValueSystem {

    public CountryProductionValueSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("CountryProductionValueSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .multiThreaded()
            .iter(this::compute);
    }

    private void compute(Iter iter) {
        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        for(int i = 0; i < iter.count(); i++) {
            CountryMarketView countryMarket = countryMarketField.getMutView(i);

            float productionValue = 0f;
            for(int g = 0; g < countryMarket.goodProducedAmountsLength(); g++) {
                productionValue += countryMarket.goodProducedAmounts(g) * countryMarket.goodPrices(g);
            }
            countryMarket.productionValue(productionValue);
        }
    }
}

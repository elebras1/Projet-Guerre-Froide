package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.CountryMarket;
import com.populaire.projetguerrefroide.component.CountryMarketView;

public class CountryRevenueSettleSystem {

    public CountryRevenueSettleSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("CountryRevenueSettleSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .multiThreaded()
            .iter(this::settle);
    }

    private void settle(Iter iter) {
        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        for(int i = 0; i < iter.count(); i++) {
            CountryMarketView countryMarket = countryMarketField.getMutView(i);
            float undistributedRevenue = countryMarket.productionValue() > 0f ? 0f : countryMarket.salesRevenue();
            countryMarket.salesRevenue(countryMarket.pendingRevenue() + undistributedRevenue);
            countryMarket.pendingRevenue(0f);
        }
    }
}

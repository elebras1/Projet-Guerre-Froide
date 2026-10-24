package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class MaintenancePurchaseSystem {

    public MaintenancePurchaseSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("MaintenancePurchaseSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .with(CountryMaintenance.class)
            .iter(this::purchase);
    }

    private void purchase(Iter iter) {
        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        Field<CountryMaintenance> countryMaintenanceField = iter.field(CountryMaintenance.class, 1);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
            CountryMaintenanceMutView countryMaintenance = countryMaintenanceField.getMutView(i);

            double paid = 0.0;
            double neededValue = 0.0;
            for (int g = 0; g < countryMaintenance.demandedAmountsLength(); g++) {
                neededValue += (double) countryMaintenance.needs(g) * countryMarket.goodPrices(g);
                float demanded = countryMaintenance.demandedAmounts(g);
                float demand = countryMarket.goodDemandAmounts(g);
                if (demanded <= 0f || demand <= 0f) {
                    continue;
                }
                float delivered = demanded * countryMarket.goodConsumedAmounts(g) / demand;
                double cost = (double) delivered * countryMarket.goodPrices(g);
                countryMarket.goodRevenues(g, countryMarket.goodRevenues(g) + cost);
                paid += cost;
            }
            countryMarket.treasury(countryMarket.treasury() - paid);
            countryMaintenance.spentToday(paid);
            countryMaintenance.satisfaction(neededValue > 0.0 ? (float) Math.min(1.0, paid / neededValue) : 1f);
        }
    }
}

package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class MaintenanceDemandSystem {

    public MaintenanceDemandSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("MaintenanceDemandSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .with(CountryBudget.class)
            .with(CountryMaintenance.class)
            .iter(this::demand);
    }

    private void demand(Iter iter) {
        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        Field<CountryBudget> countryBudgetField = iter.field(CountryBudget.class, 1);
        Field<CountryMaintenance> countryMaintenanceField = iter.field(CountryMaintenance.class, 2);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
            CountryBudgetView countryBudget = countryBudgetField.getMutView(i);
            CountryMaintenanceMutView countryMaintenance = countryMaintenanceField.getMutView(i);

            float spendingRatio = countryBudget.spendingRatio();
            for (int g = 0; g < countryMaintenance.needsLength(); g++) {
                float demanded = countryMaintenance.needs(g) * spendingRatio;
                countryMaintenance.demandedAmounts(g, demanded);
                if (demanded > 0f) {
                    countryMarket.goodDemandAmounts(g, countryMarket.goodDemandAmounts(g) + demanded);
                }
            }
        }
    }
}

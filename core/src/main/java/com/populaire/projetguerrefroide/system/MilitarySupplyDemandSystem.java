package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class MilitarySupplyDemandSystem {

    public MilitarySupplyDemandSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("MilitarySupplyDemandSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .with(CountryBudget.class)
            .with(CountryMilitary.class)
            .iter(this::demand);
    }

    private void demand(Iter iter) {
        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        Field<CountryBudget> countryBudgetField = iter.field(CountryBudget.class, 1);
        Field<CountryMilitary> countryMilitaryField = iter.field(CountryMilitary.class, 2);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
            CountryBudgetView countryBudget = countryBudgetField.getMutView(i);
            CountryMilitaryMutView countryMilitary = countryMilitaryField.getMutView(i);

            float supplyRatio = countryBudget.militarySupplyRatio();
            for (int g = 0; g < countryMilitary.supplyNeedsLength(); g++) {
                float demanded = countryMilitary.supplyNeeds(g) * supplyRatio;
                countryMilitary.demandedAmounts(g, demanded);
                if (demanded > 0f) {
                    countryMarket.goodDemandAmounts(g, countryMarket.goodDemandAmounts(g) + demanded);
                }
            }
        }
    }
}

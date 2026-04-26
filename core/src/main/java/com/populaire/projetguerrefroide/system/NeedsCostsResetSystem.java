package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.CountryMarket;
import com.populaire.projetguerrefroide.component.CountryMarketView;

public class NeedsCostsResetSystem {

    public NeedsCostsResetSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("NeedsCostsResetSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .iter(this::reset);
    }

    private void reset(Iter iter) {
        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        for(int i = 0; i < iter.count(); i++) {
            CountryMarketView countryMarket = countryMarketField.getMutView(i);
            for(int p = 0; p < countryMarket.lifeCostsByPopTypeLength(); p++) {
                countryMarket.lifeCostsByPopType(p, 0f);
                countryMarket.everydayCostsByPopType(p, 0f);
                countryMarket.luxuryCostsByPopType(p, 0f);
            }
        }
    }
}

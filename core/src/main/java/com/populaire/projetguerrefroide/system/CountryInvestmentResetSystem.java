package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.CountryInvestment;
import com.populaire.projetguerrefroide.component.CountryInvestmentMutView;

import static com.populaire.projetguerrefroide.util.Constants.OWNER_COUNT;

public class CountryInvestmentResetSystem {

    public CountryInvestmentResetSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("CountryInvestmentResetSystem")
            .kind(phaseId)
            .with(CountryInvestment.class)
            .multiThreaded()
            .iter(this::reset);
    }

    private void reset(Iter iter) {
        Field<CountryInvestment> countryInvestmentField = iter.field(CountryInvestment.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            CountryInvestmentMutView countryInvestment = countryInvestmentField.getMutView(i);
            for (int o = 0; o < OWNER_COUNT; o++) {
                countryInvestment.plannedToday(o, 0.0);
                countryInvestment.spentToday(o, 0.0);
            }
        }
    }
}

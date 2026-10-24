package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.CountryMilitary;
import com.populaire.projetguerrefroide.component.CountryMilitaryMutView;

public class MilitarySupplyResetSystem {

    public MilitarySupplyResetSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("MilitarySupplyResetSystem")
            .kind(phaseId)
            .with(CountryMilitary.class)
            .multiThreaded()
            .iter(this::reset);
    }

    private void reset(Iter iter) {
        Field<CountryMilitary> countryMilitaryField = iter.field(CountryMilitary.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            CountryMilitaryMutView countryMilitary = countryMilitaryField.getMutView(i);
            for (int g = 0; g < countryMilitary.supplyNeedsLength(); g++) {
                countryMilitary.supplyNeeds(g, 0f);
            }
        }
    }
}

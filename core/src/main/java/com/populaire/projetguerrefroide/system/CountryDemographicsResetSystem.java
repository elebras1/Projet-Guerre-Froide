package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.CountryDemographics;

public class CountryDemographicsResetSystem {
    private final World ecsWorld;

    public CountryDemographicsResetSystem(World ecsWorld, long phaseId) {
        this.ecsWorld = ecsWorld;
        ecsWorld.system("CountryDemographicsResetSystem")
            .kind(phaseId)
            .with(CountryDemographics.class)
            .iter(this::reset);
    }

    private void reset(Iter iter) {
        iter.table().resetColumn(CountryDemographics.class);
    }
}

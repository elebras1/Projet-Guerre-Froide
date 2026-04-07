package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.Demographics;

public class DemographicsResetSystem {
    private final World ecsWorld;

    public DemographicsResetSystem(World ecsWorld, long phaseId) {
        this.ecsWorld = ecsWorld;
        ecsWorld.system("DemographicsResetSystem")
            .kind(phaseId)
            .with(Demographics.class)
            .iter(this::reset);
    }

    private void reset(Iter iter) {
        iter.table().clearColumn(Demographics.class);
    }
}

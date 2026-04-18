package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.Demographics;

public class DemographicsResetSystem {

    public DemographicsResetSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("DemographicsResetSystem")
            .kind(phaseId)
            .with(Demographics.class)
            .iter(this::reset);
    }

    private void reset(Iter iter) {
        iter.table().resetColumn(Demographics.class);
    }
}

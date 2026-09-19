package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.Demographics;
import com.populaire.projetguerrefroide.component.RegionDemographics;

public class DemographicsResetSystem {

    public DemographicsResetSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("DemographicsResetSystem")
            .kind(phaseId)
            .with(Demographics.class)
            .iter(this::reset);
        ecsWorld.system("RegionDemographicsResetSystem")
            .kind(phaseId)
            .with(RegionDemographics.class)
            .iter(this::resetRegion);
    }

    private void reset(Iter iter) {
        iter.table().resetColumn(Demographics.class);
    }

    private void resetRegion(Iter iter) {
        iter.table().resetColumn(RegionDemographics.class);
    }
}

package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class EconomyBuildingHireInitializationSystem {
    private final World ecsWorld;

    public EconomyBuildingHireInitializationSystem(World ecsWorld, long phaseId) {
        this.ecsWorld = ecsWorld;
        ecsWorld.system("EconomyBuildingHireInitializationSystem")
            .kind(phaseId)
            .with(EconomyBuilding.class)
            .with(Building.class)
            .iter(this::hire);
    }

    private void hire(Iter iter) {

    }
}

package com.populaire.projetguerrefroide.simulation;

import com.populaire.projetguerrefroide.util.EcsConstants;
import io.github.elebras1.flecs.World;

public class SimulationContext {
    private final World ecsWorld;
    private final EcsConstants ecsConstants;

    public SimulationContext(World ecsWorld) {
        this.ecsWorld = ecsWorld;
        this.ecsConstants = new EcsConstants(ecsWorld);
    }

    public World getEcsWorld() {
        return this.ecsWorld;
    }

    public EcsConstants getEcsConstants() {
        return this.ecsConstants;
    }
}

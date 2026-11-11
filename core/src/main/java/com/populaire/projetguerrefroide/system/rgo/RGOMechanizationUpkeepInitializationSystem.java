package com.populaire.projetguerrefroide.system.rgo;

import com.populaire.projetguerrefroide.component.ResourceGathering;
import com.populaire.projetguerrefroide.component.ResourceGatheringMutView;
import com.populaire.projetguerrefroide.simulation.EcsConstants;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;

import static com.populaire.projetguerrefroide.simulation.Constants.RGO_MECHANIZATION_EFFICIENCY;
import static com.populaire.projetguerrefroide.simulation.Constants.RGO_MECHANIZATION_UPKEEP_RATE;

/** Upkeep of the 1946 mechanization levels, from the first production: same rule as a level completed in play. */
public class RGOMechanizationUpkeepInitializationSystem {
    private final EcsConstants ecsConstants;

    public RGOMechanizationUpkeepInitializationSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("RGOMechanizationUpkeepInitializationSystem")
            .kind(phaseId)
            .with(ResourceGathering.class)
            .iter(this::initializeUpkeep);
    }

    private void initializeUpkeep(Iter iter) {
        Field<ResourceGathering> rgoField = iter.field(ResourceGathering.class, 0);
        float[] goodCosts = this.ecsConstants.goodCosts();
        for (int i = 0; i < iter.count(); i++) {
            ResourceGatheringMutView rgo = rgoField.getMutView(i);
            if (rgo.mechanization() <= 0 || rgo.upkeepValue() > 0f || rgo.production() <= 0f) {
                continue;
            }
            float levelOutput = rgo.production() / rgo.efficiency() * RGO_MECHANIZATION_EFFICIENCY;
            rgo.upkeepValue(levelOutput * goodCosts[rgo.goodIndex()] * RGO_MECHANIZATION_UPKEEP_RATE * rgo.mechanization());
        }
    }
}

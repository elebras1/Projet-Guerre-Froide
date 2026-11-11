package com.populaire.projetguerrefroide.system.rgo;

import com.populaire.projetguerrefroide.component.*;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;

import static com.populaire.projetguerrefroide.simulation.Constants.RGO_MECHANIZATION_EFFICIENCY;

/**
 * 1946 mechanization (decision S5-c): each RGO starts at the cap its country's 1946 technologies give for its good,
 * before the first production. RGOMechanizationUpkeepInitializationSystem sets the upkeep once that production is known.
 */
public class RGOMechanizationInitializationSystem {
    public RGOMechanizationInitializationSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("RGOMechanizationInitializationSystem")
            .kind(phaseId)
            .with(ResourceGathering.class)
            .with(Province.class)
            .iter(this::initializeLevels);
    }

    private void initializeLevels(Iter iter) {
        Field<ResourceGathering> rgoField = iter.field(ResourceGathering.class, 0);
        Field<Province> provinceField = iter.field(Province.class, 1);
        long countryId = 0;
        CountryProductionTechnologyView production = null;
        for (int i = 0; i < iter.count(); i++) {
            ProvinceView province = provinceField.getMutView(i);
            if (province.ownerId() != countryId) {
                countryId = province.ownerId();
                production = iter.stage().obtainEntityView(countryId).getView(CountryProductionTechnology.class);
            }
            ResourceGatheringMutView rgo = rgoField.getMutView(i);
            int cap = production.mechanizationCaps(rgo.goodIndex());
            if (cap <= 0) {
                continue;
            }
            rgo.mechanization(cap);
            rgo.upkeepServed(1f);
            rgo.efficiency(1f + RGO_MECHANIZATION_EFFICIENCY * cap);
        }
    }
}

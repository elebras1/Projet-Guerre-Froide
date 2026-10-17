package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class PopulationRegionInitializationSystem {

    public PopulationRegionInitializationSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("PopulationRegionInitializationSystem")
            .kind(phaseId)
            .with(Population.class)
            .multiThreaded()
            .iter(this::initialize);
    }

    private void initialize(Iter iter) {
        long provinceId = 0;
        long regionInstanceId = 0;

        Field<Population> populationField = iter.field(Population.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            PopulationMutView population = populationField.getMutView(i);
            if (population.provinceId() != provinceId) {
                provinceId = population.provinceId();
                ProvinceView province = iter.world().obtainEntityView(provinceId).getMutView(Province.class);
                regionInstanceId = province.regionInstanceId();
            }
            population.regionInstanceId(regionInstanceId);
        }
    }
}

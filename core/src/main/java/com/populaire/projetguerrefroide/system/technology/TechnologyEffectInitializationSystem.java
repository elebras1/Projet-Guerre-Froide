package com.populaire.projetguerrefroide.system.technology;

import com.populaire.projetguerrefroide.component.Country;
import com.populaire.projetguerrefroide.component.CountryProductionTechnology;
import com.populaire.projetguerrefroide.component.CountryTechnology;
import com.populaire.projetguerrefroide.component.CountryTechnologyView;
import com.populaire.projetguerrefroide.simulation.EcsConstants;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;

public class TechnologyEffectInitializationSystem {
    private final EcsConstants ecsConstants;

    public TechnologyEffectInitializationSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("TechnologyEffectInitializationSystem")
            .kind(phaseId)
            .with(Country.class)
            .with(CountryTechnology.class)
            .with(CountryProductionTechnology.class)
            .iter(this::initialize);
    }

    private void initialize(Iter iter) {
        int technologyCount = this.ecsConstants.technologyCount();
        Field<CountryTechnology> countryTechnologyField = iter.field(CountryTechnology.class, 1);
        for (int i = 0; i < iter.count(); i++) {
            CountryTechnologyView countryTechnology = countryTechnologyField.getMutView(i);
            EntityView country = iter.stage().obtainEntityView(iter.entity(i));
            for (int t = 0; t < technologyCount; t++) {
                if (countryTechnology.acquired(t)) {
                    TechnologyEffectUtils.applyTechnology(this.ecsConstants, country, t);
                }
            }
            TechnologyEffectUtils.clampPolicies(country);
        }
    }
}

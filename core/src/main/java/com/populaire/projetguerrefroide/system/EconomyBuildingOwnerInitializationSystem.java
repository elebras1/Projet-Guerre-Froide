package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;

public class EconomyBuildingOwnerInitializationSystem {

    private final EcsConstants ecsConstants;

    public EconomyBuildingOwnerInitializationSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("EconomyBuildingOwnerInitializationSystem")
            .kind(phaseId)
            .with(Building.class)
            .with(EconomyBuilding.class)
            .iter(this::process);
    }

    private void process(Iter iter) {
        long countryId = 0;
        CountryEffectPolicyView countryEffectPolicy = null;

        Field<Building> buildingField = iter.field(Building.class, 0);
        Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 1);

        for (int i = 0; i < iter.count(); i++) {
            BuildingView building = buildingField.getMutView(i);
            EconomyBuildingView economyBuilding = economyBuildingField.getMutView(i);

            if (building.countryId() != countryId) {
                countryId = building.countryId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryEffectPolicy = country.getMutView(CountryEffectPolicy.class);
            }

            if(countryEffectPolicy.capitalistProfitShareRate() <= 0f) {
                economyBuilding.ownerTagId(this.ecsConstants.countryTag());
            } else {
                economyBuilding.ownerTagId(this.ecsConstants.capitalistTag());
            }
        }
    }
}

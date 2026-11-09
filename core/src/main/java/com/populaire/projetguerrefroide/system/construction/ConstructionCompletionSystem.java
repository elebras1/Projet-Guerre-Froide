package com.populaire.projetguerrefroide.system.construction;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;
import com.populaire.projetguerrefroide.system.building.InfrastructureUtils;

import static com.populaire.projetguerrefroide.system.construction.ConstructionUtils.finished;
import static com.populaire.projetguerrefroide.system.building.EconomyBuildingUtils.chooseAdvancedRecipe;
import static com.populaire.projetguerrefroide.system.building.EconomyBuildingUtils.newEconomyBuilding;

public class ConstructionCompletionSystem {

    private final EcsConstants ecsConstants;

    public ConstructionCompletionSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("ConstructionCompletionSystem")
            .kind(phaseId)
            .with(ConstructionSite.class)
            .iter(this::complete);
    }

    private void complete(Iter iter) {
        Field<ConstructionSite> constructionSiteField = iter.field(ConstructionSite.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            ConstructionSiteView site = constructionSiteField.getMutView(i);
            if (!finished(site)) {
                continue;
            }
            if (site.buildingId() == 0) {
                EntityView building = iter.stage().obtainEntityView(iter.stage().entity());
                EntityView buildingTypeEntity = iter.stage().obtainEntityView(site.buildingTypeId());
                building.isA(site.buildingTypeId());
                building.set(new Building(site.regionInstanceId(), site.countryId(), 1));
                if (buildingTypeEntity.has(SpecialBuildingType.class)) {
                    building.set(new SpecialBuilding(0f, 0f));
                } else {
                    EconomyBuildingTypeView buildingType = buildingTypeEntity.getView(EconomyBuildingType.class);
                    EntityView country = iter.stage().obtainEntityView(site.countryId());
                    boolean advancedRecipe = chooseAdvancedRecipe(buildingType, country.getView(CountryProductionTechnology.class), country.getView(CountryMarket.class));
                    building.set(newEconomyBuilding(buildingType, advancedRecipe, site.ownerTagId()));
                }
                continue;
            }
            InfrastructureUtils.levelUp(iter.stage(), this.ecsConstants, site.buildingId(), site.buildingTypeId());
        }
    }
}

package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;
import com.populaire.projetguerrefroide.util.InfrastructureUtils;

import static com.populaire.projetguerrefroide.util.ConstructionUtils.finished;
import static com.populaire.projetguerrefroide.util.EconomyBuildingUtils.newEconomyBuilding;

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
                EntityView building = iter.world().obtainEntityView(iter.world().entity());
                EconomyBuildingTypeView buildingType = iter.world().obtainEntityView(site.buildingTypeId()).getView(EconomyBuildingType.class);
                building.isA(site.buildingTypeId());
                building.set(new Building(site.regionInstanceId(), site.countryId(), 1));
                building.set(newEconomyBuilding(buildingType, false, site.ownerTagId()));
                continue;
            }
            InfrastructureUtils.levelUp(iter.world(), this.ecsConstants, site.buildingId(), site.buildingTypeId());
        }
    }
}

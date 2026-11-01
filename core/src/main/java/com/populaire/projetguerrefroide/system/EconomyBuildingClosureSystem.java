package com.populaire.projetguerrefroide.system;

import com.github.tommyettinger.ds.LongIntMap;
import com.github.tommyettinger.ds.LongList;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;
import com.populaire.projetguerrefroide.util.EconomyBuildingUtils;
import com.populaire.projetguerrefroide.util.InfrastructureUtils;

import static com.populaire.projetguerrefroide.util.Constants.FACTORY_SUSPENDED_LEVEL_DAYS;

public class EconomyBuildingClosureSystem {

    private final EcsConstants ecsConstants;
    private final Query buildingQuery;
    private final Query siteQuery;

    public EconomyBuildingClosureSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        this.buildingQuery = ecsWorld.query().with(Building.class).with(EconomyBuilding.class).with(EconomyBuildingType.class).build();
        this.siteQuery = ecsWorld.query().with(ConstructionSite.class).build();
        ecsWorld.system("EconomyBuildingClosureSystem")
            .kind(phaseId)
            .with(CountryInvestment.class)
            .run(this::close);
    }

    private void close(Iter iter) {
        World world = iter.world();
        LongList levelLosses = new LongList();
        LongList buildingTypes = new LongList();
        this.buildingQuery.iter(buildingIter -> {
            Field<EconomyBuilding> economyBuildingField = buildingIter.field(EconomyBuilding.class, 1);
            long buildingTypeId = buildingIter.src(2);
            for (int i = 0; i < buildingIter.count(); i++) {
                EconomyBuildingMutView economyBuilding = economyBuildingField.getMutView(i);
                if (!economyBuilding.suspended() || economyBuilding.suspendedDays() < FACTORY_SUSPENDED_LEVEL_DAYS) {
                    continue;
                }
                economyBuilding.suspendedDays(0);
                levelLosses.add(buildingIter.entity(i));
                buildingTypes.add(buildingTypeId);
            }
        });
        if (levelLosses.isEmpty()) {
            return;
        }

        LongIntMap sites = new LongIntMap();
        this.siteQuery.each((long siteId) -> {
            ConstructionSiteView site = world.obtainEntityView(siteId).getView(ConstructionSite.class);
            if (site.buildingId() != 0) {
                sites.put(site.buildingId(), 1);
            }
        });

        for (int b = 0; b < levelLosses.size(); b++) {
            long buildingId = levelLosses.get(b);
            BuildingView building = world.obtainEntityView(buildingId).getView(Building.class);
            if (building.size() > 1) {
                InfrastructureUtils.levelDown(world, buildingId, buildingTypes.get(b));
            } else if (!sites.containsKey(buildingId)) {
                EconomyBuildingUtils.releaseAssets(world, this.ecsConstants, buildingId);
                world.obtainEntity(buildingId).destruct();
            }
        }
    }
}

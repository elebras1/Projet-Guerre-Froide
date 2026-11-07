package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.callback.IterCallback;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;

public class SpecialBuildingPostSystem {

    private final Query specialQuery;
    private final EcsConstants ecsConstants;
    private final IterCallback readSpecialReader = this::readSpecial;
    private World world;
    private int postIndex;

    public SpecialBuildingPostSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        this.specialQuery = ecsWorld.query()
            .with(Building.class)
            .with(SpecialBuilding.class)
            .with(SpecialBuildingType.class)
            .build();
        ecsWorld.system("SpecialBuildingPostSystem")
            .kind(phaseId)
            .with(RegionSpecialBuildings.class)
            .run(this::count);
    }

    private void count(Iter iter) {
        World world = iter.world();
        this.world = world;
        GlobalPopulationTypeView globalPopulationType = world.obtainEntityView(this.ecsConstants.globalPopulationType()).getView(GlobalPopulationType.class);
        int postIndex = globalPopulationType.statePostPopTypeIndex();
        while (iter.next()) {
            Field<RegionSpecialBuildings> regionField = iter.field(RegionSpecialBuildings.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                RegionSpecialBuildingsMutView region = regionField.getMutView(i);
                region.staffPosts(0f);
            }
        }

        this.postIndex = postIndex;
        this.specialQuery.iter(this.readSpecialReader);
    }

    private void readSpecial(Iter specialIter) {
        World world = this.world;
        int postIndex = this.postIndex;
        SpecialBuildingTypeView buildingType = specialIter.field(SpecialBuildingType.class, 2).getView(0);
        if (postIndex < 0 || buildingType.staffPopTypeIndex() != postIndex || buildingType.staffPosts() <= 0f) {
            return;
        }
        Field<Building> buildingField = specialIter.field(Building.class, 0);
        for (int j = 0; j < specialIter.count(); j++) {
            BuildingView building = buildingField.getMutView(j);
            if (building.size() <= 0 || building.parentId() == 0) {
                continue;
            }
            RegionSpecialBuildingsMutView region = world.obtainEntityView(building.parentId()).getMutView(RegionSpecialBuildings.class);
            region.staffPosts(region.staffPosts() + building.size() * buildingType.staffPosts());
        }
    }
}

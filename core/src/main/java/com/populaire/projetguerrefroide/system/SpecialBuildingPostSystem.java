package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class SpecialBuildingPostSystem {

    private final Query specialQuery;

    public SpecialBuildingPostSystem(World ecsWorld, long phaseId) {
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
        GlobalPopulationTypeView globalPopulationType = world.obtainEntityView(world.lookup("global_population_type")).getView(GlobalPopulationType.class);
        int postIndex = globalPopulationType.statePostPopTypeIndex();
        while (iter.next()) {
            Field<RegionSpecialBuildings> regionField = iter.field(RegionSpecialBuildings.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                RegionSpecialBuildingsMutView region = regionField.getMutView(i);
                region.staffPosts(0f);
            }
        }

        this.specialQuery.iter(specialIter -> {
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
        });
    }
}

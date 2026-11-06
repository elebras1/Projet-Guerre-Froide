package com.populaire.projetguerrefroide.simulation.request;

import com.populaire.projetguerrefroide.component.Building;
import com.populaire.projetguerrefroide.component.BuildingView;
import com.populaire.projetguerrefroide.protocol.SimulationHeader;
import com.populaire.projetguerrefroide.protocol.request.RequestParams;
import com.populaire.projetguerrefroide.protocol.response.MapBuildings;
import com.populaire.projetguerrefroide.simulation.SimulationContext;
import com.populaire.projetguerrefroide.simulation.SimulationState;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Flecs;
import io.github.elebras1.flecs.FlecsSystem;
import io.github.elebras1.flecs.Iter;

public final class MapBuildingsHandler extends RequestHandler<MapBuildings> {
    private final FlecsSystem buildingsSystem;
    private MapBuildings response;

    public MapBuildingsHandler(SimulationContext simulationContext, SimulationState state, SimulationHeader header) {
        super(simulationContext, state, header);
        this.buildingsSystem = this.ecsWorld.system("MapBuildingsRequest")
            .kind(0)
            .with(Building.class)
            .with(this.ecsConstants.onMap())
            .iter(this::collect);
    }

    @Override
    protected boolean fill(RequestParams params, MapBuildings response) {
        response.clear();
        this.response = response;
        this.buildingsSystem.run();
        this.response = null;
        return true;
    }

    private void collect(Iter iter) {
        Field<Building> buildingField = iter.field(Building.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            BuildingView building = buildingField.getMutView(i);
            int provinceIndex = this.catalog.provinces().indexOf(building.parentId());
            if (provinceIndex < 0) {
                continue;
            }
            long typeId = this.ecsWorld.obtainEntityView(iter.entity(i)).target(Flecs.IsA, 0);
            int typeIndex = this.catalog.buildingTypes().indexOf(typeId);
            if (typeIndex >= 0) {
                this.response.add(provinceIndex, typeIndex);
            }
        }
    }
}

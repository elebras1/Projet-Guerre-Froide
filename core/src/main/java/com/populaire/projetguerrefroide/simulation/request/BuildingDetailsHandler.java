package com.populaire.projetguerrefroide.simulation.request;

import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.protocol.SimulationHeader;
import com.populaire.projetguerrefroide.protocol.request.RequestParams;
import com.populaire.projetguerrefroide.protocol.response.BuildingDetails;
import com.populaire.projetguerrefroide.simulation.SimulationContext;
import com.populaire.projetguerrefroide.simulation.SimulationState;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Flecs;

public final class BuildingDetailsHandler extends RequestHandler<BuildingDetails> {

    public BuildingDetailsHandler(SimulationContext simulationContext, SimulationState state, SimulationHeader header) {
        super(simulationContext, state, header);
    }

    @Override
    protected boolean fill(RequestParams params, BuildingDetails response) {
        long buildingId = params.id();
        response.clear(buildingId);
        if (buildingId == 0 || !this.ecsWorld.isAlive(buildingId)) {
            return false;
        }
        EntityView building = this.ecsWorld.obtainEntityView(buildingId);
        if (!building.has(EconomyBuilding.class) || !building.has(Building.class)) {
            return false;
        }
        BuildingView buildingData = building.getMutView(Building.class);
        int size = buildingData.size();
        long parentId = buildingData.parentId();
        EconomyBuildingView economyBuilding = building.getMutView(EconomyBuilding.class);
        int workers = economyBuilding.primaryWorkerAmount() + economyBuilding.secondaryWorkerAmount();
        boolean stateControlled = economyBuilding.ownerTagId() == this.ecsConstants.countryTag();
        boolean suspended = building.has(this.ecsConstants.suspended()) || economyBuilding.suspended();
        long typeId = building.target(Flecs.IsA, 0);
        int typeIndex = this.catalog.buildingTypes().indexOf(typeId);

        int regionIndex = -1;
        if (parentId != 0) {
            EntityView parent = this.ecsWorld.obtainEntityView(parentId);
            if (parent.has(RegionInstance.class)) {
                regionIndex = this.catalog.regions().indexOf(parent.<RegionInstanceMutView>getMutView(RegionInstance.class).regionId());
            }
        }

        EconomyBuildingTypeView buildingType = this.ecsWorld.obtainEntityView(typeId).getView(EconomyBuildingType.class);
        for (int i = 0; i < buildingType.goodInputIdsLength(); i++) {
            long goodId = buildingType.goodInputIds(i);
            if (goodId != 0) {
                response.addInputGood(this.catalog.goods().indexOf(goodId));
            }
        }
        long outputGoodId = buildingType.goodOutputId();
        int outputGoodIndex = outputGoodId == 0 ? -1 : this.catalog.goods().indexOf(outputGoodId);
        response.set(typeIndex, regionIndex, buildingType.maxLevel(), outputGoodIndex, buildingType.goodOutputAmount(), workers, size * buildingType.workforce(), suspended, stateControlled);
        return true;
    }
}

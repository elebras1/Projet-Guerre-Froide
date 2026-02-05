package com.populaire.projetguerrefroide.command.handler;

import com.populaire.projetguerrefroide.command.request.DemolishBuildingCommand;
import com.populaire.projetguerrefroide.service.BuildingService;

public class DemolishBuildingHandler implements CommandHandler<DemolishBuildingCommand> {
    private final BuildingService buildingService;

    public DemolishBuildingHandler(final BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    @Override
    public void handle(DemolishBuildingCommand command) {
        // TODO
        System.out.println("Demolish Building : " + command.buildingId());
    }
}

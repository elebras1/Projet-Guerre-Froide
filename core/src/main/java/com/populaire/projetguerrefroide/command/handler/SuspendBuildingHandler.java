package com.populaire.projetguerrefroide.command.handler;

import com.populaire.projetguerrefroide.command.request.SuspendBuildingCommand;
import com.populaire.projetguerrefroide.service.BuildingService;

public class SuspendBuildingHandler implements CommandHandler<SuspendBuildingCommand> {
    private final BuildingService buildingService;

    public SuspendBuildingHandler(final BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    @Override
    public void handle(SuspendBuildingCommand command) {
        this.buildingService.suspendBuilding(command.buildingId());
    }
}

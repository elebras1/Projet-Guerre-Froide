package com.populaire.projetguerrefroide.command.handler;

import com.populaire.projetguerrefroide.command.request.ResumeBuildingCommand;
import com.populaire.projetguerrefroide.service.BuildingService;

public class ResumeBuildingHandler implements CommandHandler<ResumeBuildingCommand> {
    private final BuildingService buildingService;

    public ResumeBuildingHandler(BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    @Override
    public void handle(ResumeBuildingCommand command) {
        this.buildingService.resumeBuilding(command.buildingId());
    }
}

package com.populaire.projetguerrefroide.command.handler;

import com.populaire.projetguerrefroide.command.request.ExpandBuildingCommand;
import com.populaire.projetguerrefroide.service.BuildingService;

public class ExpandBuildingHandler implements CommandHandler<ExpandBuildingCommand> {
    private final BuildingService buildingService;

    public ExpandBuildingHandler(final BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    @Override
    public void handle(ExpandBuildingCommand command) {
        // TODO
        System.out.println("Expanding Building : " + command.buildingId());
    }
}

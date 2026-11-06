package com.populaire.projetguerrefroide.protocol.command;

public record DemolishBuildingCommand(long buildingId) implements GameCommand {
}

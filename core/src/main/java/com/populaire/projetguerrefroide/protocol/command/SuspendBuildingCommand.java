package com.populaire.projetguerrefroide.protocol.command;

public record SuspendBuildingCommand(long buildingId) implements GameCommand {
}

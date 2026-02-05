package com.populaire.projetguerrefroide.command.request;

public record SuspendBuildingCommand(long buildingId) implements Command {
}

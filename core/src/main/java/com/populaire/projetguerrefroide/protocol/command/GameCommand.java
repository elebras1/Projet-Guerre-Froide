package com.populaire.projetguerrefroide.protocol.command;

public sealed interface GameCommand permits ChoosePlayerCountryCommand, ExpandBuildingCommand, SuspendBuildingCommand, ResumeBuildingCommand, DemolishBuildingCommand {
}

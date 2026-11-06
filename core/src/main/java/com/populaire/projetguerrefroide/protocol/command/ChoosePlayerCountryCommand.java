package com.populaire.projetguerrefroide.protocol.command;

public record ChoosePlayerCountryCommand(long countryId) implements GameCommand {
}

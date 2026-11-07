package com.populaire.projetguerrefroide.screen;

public record LobbySetup(boolean multiplayer, String gameName, String playerName) {

    public static LobbySetup singleplayer() {
        return new LobbySetup(false, "", "");
    }
}

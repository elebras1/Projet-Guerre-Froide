package com.populaire.projetguerrefroide.ui.view.lobby;

public interface LobbyListener {
    void onPlayClicked();

    void onBackClicked();

    void onChatMessage(String message);
}

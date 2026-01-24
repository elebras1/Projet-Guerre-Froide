package com.populaire.projetguerrefroide.screen;

public interface GameFlowHandler {
    void pause();
    void resume();
    void setInputEnabled(boolean enabled);
    void toggleEconomyPanel();
}

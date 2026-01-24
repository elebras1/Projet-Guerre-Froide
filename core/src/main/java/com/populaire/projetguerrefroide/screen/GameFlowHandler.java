package com.populaire.projetguerrefroide.screen;

public interface GameFlowHandler {
    void pause();
    void resume();
    void setInputEnabled(boolean enabled);
    void toggleEconomyPanel();
    void showTooltip(String content);
    void hideTooltip();
    void moveCameraTo(int x, int y);
    void zoomIn();
    void zoomOut();
}

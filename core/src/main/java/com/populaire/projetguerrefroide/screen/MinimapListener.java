package com.populaire.projetguerrefroide.screen;

public interface MinimapListener {
    void moveCamera(short x, short y);
    void zoomIn();
    void zoomOut();
    void changeMapMode(String mapMode);
    void updateHoverBox(String text);
    void hideHoverBox();
}

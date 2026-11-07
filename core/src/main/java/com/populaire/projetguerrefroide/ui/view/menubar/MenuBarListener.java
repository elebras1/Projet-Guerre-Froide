package com.populaire.projetguerrefroide.ui.view.menubar;

public interface MenuBarListener {
    void moveCamera(int x, int y);

    void zoomIn();

    void zoomOut();

    void changeMapMode(String mapMode);

    void updateHoverTooltip(String content);

    void hideUiTooltip();
}

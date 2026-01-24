package com.populaire.projetguerrefroide.screen.listener;

import com.github.tommyettinger.ds.IntObjectMap;

public interface MinimapListener {
    void moveCamera(short x, short y);
    void zoomIn();
    void zoomOut();
    void changeMapMode(String mapMode);
    IntObjectMap<String> getInformationsMapMode(String mapMode);
    void updateHoverTooltip(String text);
    void hideHoverBox();
}

package com.populaire.projetguerrefroide.ui.view.topbar;

public interface TopBarListener {
    int onSpeedUp();

    int onSpeedDown();

    int onTogglePause();

    void onEconomyClicked();

    void onStatisticsClicked();

    void onMenuClicked();
}

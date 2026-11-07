package com.populaire.projetguerrefroide.ui.view.mainmenu;

import com.populaire.projetguerrefroide.configuration.Settings;

public interface MainMenuListener {
    void onSinglePlayerClicked();

    void onExitClicked();

    Settings onShowSettingsClicked();

    void onApplySettingsClicked(Settings settings);

    void onHostClicked(String gameName, String playerName);

    void onJoinLanClicked(String playerName);

    void onJoinInternetClicked(String address, String playerName);

    void onScanClicked();

    void onMetaserverClicked();

    void onTutorialPlayClicked(int category, int chapter);
}

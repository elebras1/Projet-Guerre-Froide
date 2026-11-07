package com.populaire.projetguerrefroide.ui.view;

import com.populaire.projetguerrefroide.configuration.Settings;

public interface MainMenuInGameListener {
    Settings onShowSettingsClicked();
    void onApplySettingsClicked(Settings settings);
    void onCloseMainMenuInGameClicked();
    void onQuitClicked(PopupListener listener);
    void onOkPopupClicked();
    void onCancelPopupClicked();
}

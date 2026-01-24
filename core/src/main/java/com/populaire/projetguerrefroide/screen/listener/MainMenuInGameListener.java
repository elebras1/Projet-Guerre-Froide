package com.populaire.projetguerrefroide.screen.listener;

import com.populaire.projetguerrefroide.configuration.Settings;
import com.populaire.projetguerrefroide.ui.view.PopupListener;

public interface MainMenuInGameListener {
    Settings onShowSettingsClicked();
    void onApplySettingsClicked(Settings settings);
    void onCloseMainMenuInGameClicked();
    void onQuitClicked(PopupListener listener);
    void onOkPopupClicked();
    void onCancelPopupClicked();
}

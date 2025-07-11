package com.populaire.projetguerrefroide.screen;

import com.populaire.projetguerrefroide.configuration.Settings;
import com.populaire.projetguerrefroide.ui.view.PopupListener;

public interface MainMenuInGameListener {
    Settings onShowSettingsClicked();
    void onApplySettingsClicked(Settings settings);
    void onCloseClicked();
    void onQuitClicked(PopupListener listener);
    void onOkPopupClicked();
    void onCancelPopupClicked();
}

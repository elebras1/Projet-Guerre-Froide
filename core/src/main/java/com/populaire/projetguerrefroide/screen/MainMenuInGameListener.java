package com.populaire.projetguerrefroide.screen;

import com.populaire.projetguerrefroide.configuration.Settings;
import com.populaire.projetguerrefroide.ui.PopupListener;

public interface MainMenuInGameListener {
    void onApplySettings(Settings settings);
    void onCloseClicked();
    void onQuitClicked(PopupListener listener);
    void onOkPopupClicked();
    void onCancelPopupClicked();
}

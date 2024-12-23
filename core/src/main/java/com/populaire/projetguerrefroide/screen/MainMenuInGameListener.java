package com.populaire.projetguerrefroide.screen;

import com.populaire.projetguerrefroide.ui.PopupListener;

public interface MainMenuInGameListener {
    void onCloseClicked();
    void onQuitClicked(PopupListener listener);
    void onOkPopupClicked();
    void onCancelPopupClicked();
}

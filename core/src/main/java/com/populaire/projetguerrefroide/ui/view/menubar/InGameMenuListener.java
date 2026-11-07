package com.populaire.projetguerrefroide.ui.view.menubar;

public interface InGameMenuListener {
    void onSaveClicked(String fileName);

    void onGameOptionsClicked();

    void onResignClicked();

    void onQuitClicked();

    void onCloseClicked();
}

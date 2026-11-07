package com.populaire.projetguerrefroide.ui.view.lobby;

import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiList;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class LobbySaveGames extends UiWindow {
    private final UiList<SavegameEntry> saveGamesList;

    public LobbySaveGames(UiBuilder ui) {
        super(ui, 273f, 768f);
        this.saveGamesList = ui.list(this, 3f, 48f, 271f, 276f, UiAnchor.UPPER_LEFT, 1f, 0f, 0f, SavegameEntry::new);
    }

    public UiList<SavegameEntry> list() {
        return this.saveGamesList;
    }
}

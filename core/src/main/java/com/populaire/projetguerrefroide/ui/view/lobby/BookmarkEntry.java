package com.populaire.projetguerrefroide.ui.view.lobby;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.configuration.Bookmark;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiNewgameSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class BookmarkEntry extends UiWindow {
    private final Button bookmark;
    private final Label title;
    private final Label date;

    public BookmarkEntry(UiBuilder ui) {
        super(ui, 320f, 65f);
        Skin newGameSkin = ui.skin(Skins.UI_NEWGAME);
        this.bookmark = ui.flatButton(this, newGameSkin, UiNewgameSkin.Drawables.SCENARIO_ENTRY, -4f, -1f, UiAnchor.UPPER_LEFT);
        this.title = ui.label(this, "", FontsSkin.Styles.Label.IMPACT_SMALL, 9f, 10f, 320f, 18f, Align.topLeft, UiAnchor.UPPER_LEFT, 1f, 1f);
        this.date = ui.label(this, "", FontsSkin.Styles.Label.FRANGOTH_BOLD, 7f, 27f, 320f, 18f, Align.topLeft, UiAnchor.UPPER_LEFT, 1f, 1f);
    }

    public void bind(Bookmark bookmark) {
        Skin newGameSkin = this.ui.skin(Skins.UI_NEWGAME);
        Button.ButtonStyle style = this.bookmark.getStyle();
        style.up = newGameSkin.getDrawable(bookmark.iconNameFile());
        this.bookmark.setStyle(style);
        this.bookmark.setSize(this.bookmark.getPrefWidth(), this.bookmark.getPrefHeight());
        this.invalidate();
        this.title.setText(this.ui.localise(bookmark.nameId()));
        this.date.setText(this.ui.localise(bookmark.descriptionId()));
    }
}

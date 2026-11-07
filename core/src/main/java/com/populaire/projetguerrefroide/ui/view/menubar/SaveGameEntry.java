package com.populaire.projetguerrefroide.ui.view.menubar;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiNewgameSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.FlagImage;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class SaveGameEntry extends UiWindow {
    private final Button game;
    private final FlagImage playerShield;
    private final Label title;
    private final Label date;

    public SaveGameEntry(UiBuilder ui) {
        super(ui, 264f, 54f);
        Skin newGameSkin = ui.skin(Skins.UI_NEWGAME);
        Skin uiSkin = ui.skin(Skins.UI);
        this.game = ui.button(this, newGameSkin, UiNewgameSkin.Styles.Button.FRONTEND_SAVEGAME_ENTRY_TGL, -3f, -3f, UiAnchor.UPPER_LEFT);
        this.playerShield = ui.flag(this, uiSkin, UiSkin.Drawables.MINIMASK, UiSkin.Drawables.MINISHIELD, 9f, 11f, UiAnchor.UPPER_LEFT);
        this.title = ui.label(this, "", FontsSkin.Styles.Label.ARIAL12_BOLD, 67f, 5f, 200f, 18f, Align.topRight, UiAnchor.UPPER_LEFT, 1f, 1f);
        this.date = ui.label(this, "", FontsSkin.Styles.Label.ARIAL12, 87f, 27f, 180f, 18f, Align.topRight, UiAnchor.UPPER_LEFT, 1f, 1f);
    }
}

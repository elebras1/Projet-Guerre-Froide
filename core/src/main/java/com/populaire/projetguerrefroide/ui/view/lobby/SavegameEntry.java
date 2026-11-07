package com.populaire.projetguerrefroide.ui.view.lobby;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
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

public class SavegameEntry extends UiWindow {
    private final Button saveGame;
    private final FlagImage shield;
    private final Label title;
    private final Label date;

    public SavegameEntry(UiBuilder ui) {
        super(ui, 264f, 54f);
        Skin newGameSkin = ui.skin(Skins.UI_NEWGAME);
        Skin uiSkin = ui.skin(Skins.UI);
        this.saveGame = ui.button(this, newGameSkin, UiNewgameSkin.Styles.Button.FRONTEND_SAVEGAME_ENTRY_TGL, -3f, -3f, UiAnchor.UPPER_LEFT);
        this.shield = ui.flag(this, uiSkin, UiSkin.Drawables.MINIMASK, UiSkin.Drawables.MINISHIELD, 9f, 11f, UiAnchor.UPPER_LEFT);
        this.title = ui.label(this, "", FontsSkin.Styles.Label.ARIAL12_BOLD, 75f, 5f, 180f, 18f, Align.topRight, UiAnchor.UPPER_LEFT, 1f, 1f);
        this.date = ui.label(this, "", FontsSkin.Styles.Label.ARIAL12, 75f, 27f, 180f, 18f, Align.topRight, UiAnchor.UPPER_LEFT, 1f, 1f);
    }

    public void bind(String title, String date, TextureRegion flag) {
        this.title.setText(title);
        this.date.setText(date);
        this.shield.setFlag(flag);
    }

    public Button button() {
        return this.saveGame;
    }
}

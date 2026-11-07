package com.populaire.projetguerrefroide.ui.view.lobby;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
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

public class MultiplayerEntry extends UiWindow {
    private final Image frontendPlayerEntry;
    private final FlagImage playerShield;
    private final Label name;
    private final Label saveProgress;

    public MultiplayerEntry(UiBuilder ui) {
        super(ui, 264f, 32f);
        Skin newGameSkin = ui.skin(Skins.UI_NEWGAME);
        Skin uiSkin = ui.skin(Skins.UI);
        this.frontendPlayerEntry = ui.image(this, newGameSkin, UiNewgameSkin.Drawables.PLAYER_ENTRY, 0f, -2f, UiAnchor.UPPER_LEFT);
        this.playerShield = ui.flag(this, uiSkin, UiSkin.Drawables.MINIMASK, UiSkin.Drawables.MINISHIELD, 1f, 0f, UiAnchor.UPPER_LEFT);
        this.name = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 39f, 8f, 210f, 32f, Align.topRight, UiAnchor.UPPER_LEFT);
        this.saveProgress = ui.label(this, "", FontsSkin.Styles.Label.ARIAL12, 46f, 7f, 210f, 32f, Align.topRight, UiAnchor.UPPER_LEFT);
    }

    public void bind(String name, TextureRegion flag) {
        this.name.setText(name);
        this.playerShield.setFlag(flag);
        this.saveProgress.setText("");
    }
}

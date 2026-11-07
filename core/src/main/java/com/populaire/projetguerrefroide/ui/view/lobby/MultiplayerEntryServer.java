package com.populaire.projetguerrefroide.ui.view.lobby;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
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

public class MultiplayerEntryServer extends UiWindow {
    private final Image frontendPlayerEntry;
    private final FlagImage playerShield;
    private final Label name;
    private final Button buttonKick;
    private final Button buttonBan;
    private final Label saveProgress;

    public MultiplayerEntryServer(UiBuilder ui) {
        super(ui, 264f, 32f);
        Skin newGameSkin = ui.skin(Skins.UI_NEWGAME);
        Skin uiSkin = ui.skin(Skins.UI);
        this.frontendPlayerEntry = ui.image(this, newGameSkin, UiNewgameSkin.Drawables.PLAYER_ENTRY, -2f, -2f, UiAnchor.UPPER_LEFT);
        this.playerShield = ui.flag(this, uiSkin, UiSkin.Drawables.MINIMASK, UiSkin.Drawables.MINISHIELD, 7f, 1f, UiAnchor.UPPER_LEFT);
        this.name = ui.label(this, "", FontsSkin.Styles.Label.ARIAL_14_BOLD_GLOW, 46f, 7f, 210f, 32f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.buttonKick = ui.button(this, newGameSkin, UiNewgameSkin.Styles.Button.BUTTON_KICK_SMALL_BTN, 210f, 4f, UiAnchor.UPPER_LEFT);
        this.buttonBan = ui.button(this, newGameSkin, UiNewgameSkin.Styles.Button.BUTTON_BAN_SMALL_BTN, 232f, 4f, UiAnchor.UPPER_LEFT);
        this.saveProgress = ui.label(this, "", FontsSkin.Styles.Label.ARIAL12, 46f, 7f, 210f, 32f, Align.topLeft, UiAnchor.UPPER_LEFT);
    }

    public void bind(String name, TextureRegion flag, boolean canBeKicked) {
        this.name.setText(name);
        this.playerShield.setFlag(flag);
        this.playerShield.setVisible(flag != null);
        this.buttonKick.setVisible(canBeKicked);
        this.buttonBan.setVisible(canBeKicked);
        this.saveProgress.setText("");
    }

    public Button kickButton() {
        return this.buttonKick;
    }

    public Button banButton() {
        return this.buttonBan;
    }
}

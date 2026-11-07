package com.populaire.projetguerrefroide.ui.view.lobby;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiNewgameSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiList;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class LobbyMultiplayer extends UiWindow {
    private final Image frontendPlayersBg;
    private final UiList<MultiplayerEntryServer> multiplayerList;
    private final Label numPlayers;
    private String hostName;

    public LobbyMultiplayer(UiBuilder ui) {
        super(ui, 200f, 300f);
        Skin newGameSkin = ui.skin(Skins.UI_NEWGAME);
        this.frontendPlayersBg = ui.image(this, newGameSkin, UiNewgameSkin.Drawables.PLAYERS_BG, -12f, 47f, UiAnchor.UPPER_LEFT);
        this.multiplayerList = ui.list(this, -10f, 84f, 264f, 162f, UiAnchor.UPPER_LEFT, 0f, 0f, 0f, MultiplayerEntryServer::new);
        this.numPlayers = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14_GLOW_BLUE, -2f, 57f, 180f, 24f, Align.topLeft, UiAnchor.UPPER_LEFT, 0f, 4f);
    }

    public void setHost(String playerName, TextureRegion flag) {
        this.hostName = playerName;
        this.multiplayerList.setRowCount(1);
        this.multiplayerList.row(0).bind(playerName, flag, false);
        this.numPlayers.setText(this.ui.localise("FE_PLAYER") + " : 1");
    }

    public void setHostFlag(TextureRegion flag) {
        this.multiplayerList.row(0).bind(this.hostName, flag, false);
    }
}

package com.populaire.projetguerrefroide.ui.view.mainmenu;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiMainmenuSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiList;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;
import java.util.List;

public class MultiplayerMenu extends UiWindow {
    private final Image bgMultiplayerMenu;
    private final UiList<LocalGameEntry> games;
    private final Button host;
    private final Button join;
    private final Button joinDirect;
    private final Button scan;
    private final Button metaserver;
    private final Button back;
    private final TextField playername;
    private final Label nameLabel;
    private final ButtonGroup<Button> selectedGame;

    public MultiplayerMenu(UiBuilder ui, MainMenu menu) {
        super(ui, 420f, 500f);
        Skin mainMenuSkin = ui.skin(Skins.UI_MAINMENU);
        Skin uiSkin = ui.skin(Skins.UI);
        this.bgMultiplayerMenu = ui.image(this, mainMenuSkin, UiMainmenuSkin.Drawables.BG_MULTIPLAYER_MENU, 129f, 79f, UiAnchor.UPPER_LEFT);
        this.games = ui.list(this, 130f, 122f, 280f, 89f, UiAnchor.UPPER_LEFT, 2f, 20f, 20f, LocalGameEntry::new);
        this.host = ui.button(this, mainMenuSkin, UiMainmenuSkin.Styles.Button.FRONTEND_BUTTON_BTN, 123f, 251f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.host, "FE_HOST", FontsSkin.Styles.Label.ARIAL_20);
        this.join = ui.button(this, mainMenuSkin, UiMainmenuSkin.Styles.Button.FRONTEND_BUTTON_BTN, 123f, 294f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.join, "FE_JOIN_LAN_GAME", FontsSkin.Styles.Label.ARIAL_20);
        this.joinDirect = ui.button(this, mainMenuSkin, UiMainmenuSkin.Styles.Button.FRONTEND_BUTTON_BTN, 123f, 337f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.joinDirect, "FE_JOIN_INTERNET_GAME", FontsSkin.Styles.Label.ARIAL_20);
        this.scan = ui.button(this, mainMenuSkin, UiMainmenuSkin.Styles.Button.FRONTEND_BUTTON5_BTN, 260f, 207f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.scan, "FE_SCAN", FontsSkin.Styles.Label.FRANGOTH_BOLD);
        this.metaserver = ui.button(this, mainMenuSkin, UiMainmenuSkin.Styles.Button.FRONTEND_BUTTON_BTN, 123f, 380f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.metaserver, "FE_METASERVER", FontsSkin.Styles.Label.ARIAL_20);
        this.back = ui.button(this, mainMenuSkin, UiMainmenuSkin.Styles.Button.FRONTEND_BUTTON2_BTN, 123f, 431f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.back, "BACK", FontsSkin.Styles.Label.ARIAL_20);
        this.playername = ui.textField(this, uiSkin, UiSkin.Drawables.SMALL_TILES_DIALOG, "FE_PLAYER", FontsSkin.Styles.Label.ARIAL12_HEADLINE, 210f, 80f, 213f, 32f, UiAnchor.UPPER_LEFT, 8f, 4f);
        this.nameLabel = ui.label(this, "FE_PLAYER_NAME", FontsSkin.Styles.Label.ARIAL12_BOLD, 137f, 88f, 200f, 100f, Align.topLeft, UiAnchor.UPPER_LEFT, 4f, 4f);
        this.games.setHeader(new LocalGameText(ui));
        this.selectedGame = new ButtonGroup<>();
        this.selectedGame.setMinCheckCount(0);
        ui.onClick(this.host, () -> menu.showHostNaming(this.playerName()));
        ui.onClick(this.join, () -> menu.onJoinLanClicked(this.playerName()));
        ui.onClick(this.joinDirect, () -> menu.showJoinNaming(this.playerName()));
        ui.onClick(this.scan, menu::onScanClicked);
        ui.onClick(this.metaserver, menu::onMetaserverClicked);
        ui.onClick(this.back, menu::showPanel);
    }

    public void setGames(List<String> names) {
        this.selectedGame.clear();
        this.games.setRowCount(names.size());
        for (int i = 0; i < names.size(); i++) {
            LocalGameEntry entry = this.games.row(i);
            entry.setName(names.get(i));
            this.selectedGame.add(entry.button());
        }
    }

    private String playerName() {
        return this.playername.getText();
    }
}

package com.populaire.projetguerrefroide.ui.view.lobby;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.configuration.Bookmark;
import com.populaire.projetguerrefroide.protocol.response.CountrySummary;
import com.populaire.projetguerrefroide.ui.UiCatalog;
import com.populaire.projetguerrefroide.ui.format.TextFormat;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiNewgameSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiList;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;
import java.util.List;

public class Lobby extends UiWindow {
    private final Label checksum;
    private final Image frontendChatBg;
    private final TextField lobbyChatEdit;
    private final Image frontendTabsBg;
    private final Button newgameTab;
    private final Label bookmarksLabel;
    private final Button savedgameTab;
    private final Label savedgamesLabel;
    private final Image frontendSelectedScenarioBg;
    private final Label yearLabel;
    private final Label selectLabel;
    private final LobbyMultiplayer multiplayer;
    private final LobbySingleplayer singleplayer;
    private final LobbySaveGames saveGames;
    private final LobbyBookmarks bookmarks;
    private final Button backButton;
    private final Label backLabel;
    private final Button playButton;
    private final Label playLabel;
    private final UiList<LobbyChatText> chatlog;
    private final boolean multiplayerMode;

    public Lobby(UiBuilder ui, LobbyListener listener, UiCatalog uiCatalog, TextFormat textFormat, Bookmark bookmark, boolean multiplayerMode) {
        super(ui);
        this.multiplayerMode = multiplayerMode;
        Skin newGameSkin = ui.skin(Skins.UI_NEWGAME);
        Skin uiSkin = ui.skin(Skins.UI);
        this.checksum = ui.label(this, "", FontsSkin.Styles.Label.ARIAL12, -510f, -221f, 180f, 24f, Align.topLeft, UiAnchor.CENTER_DOWN, 0f, 4f);
        this.frontendChatBg = ui.image(this, newGameSkin, UiNewgameSkin.Drawables.BOTTOM_BG, -524f, -209f, UiAnchor.CENTER_DOWN);
        this.lobbyChatEdit = ui.textField(this, uiSkin, UiSkin.Drawables.SMALL_TILES_DIALOG, "", FontsSkin.Styles.Label.JOCKEY_14, -510f, -45f, 730f, 32f, UiAnchor.CENTER_DOWN, 8f, 4f);
        this.frontendTabsBg = ui.image(this, newGameSkin, UiNewgameSkin.Drawables.TAB_BG, -2f, 0f, UiAnchor.UPPER_LEFT);
        this.newgameTab = ui.button(this, newGameSkin, UiNewgameSkin.Styles.Button.TAB_BTN, 9f, 10f, UiAnchor.UPPER_LEFT);
        this.bookmarksLabel = ui.buttonLabel(this.newgameTab, "HISTORICAL_START", FontsSkin.Styles.Label.JOCKEY_16_DARK);
        this.savedgameTab = ui.button(this, newGameSkin, UiNewgameSkin.Styles.Button.TAB_BTN, 136f, 10f, UiAnchor.UPPER_LEFT);
        this.savedgamesLabel = ui.buttonLabel(this.savedgameTab, "SAVED_GAMES", FontsSkin.Styles.Label.JOCKEY_16_DARK);
        // front_doomsday_clock : GFX_frontend_doomsday_clock (Doomsday Clock écartée du projet)
        this.frontendSelectedScenarioBg = ui.image(this, newGameSkin, UiNewgameSkin.Drawables.SELECTED_SCENARIO_BG, -220f, -2f, UiAnchor.CENTER_UP);
        this.yearLabel = ui.label(this, "TITLE_SELECT_NATION", FontsSkin.Styles.Label.JOCKEY_18_BLACK, -288f, 19f, 600f, 30f, Align.top, UiAnchor.CENTER_UP, 4f, 4f);
        this.selectLabel = ui.label(this, "NATION_TO_PLAY", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK, -145f, 50f, 300f, 30f, Align.top, UiAnchor.CENTER_UP, 4f, 4f);
        this.multiplayer = ui.add(this, new LobbyMultiplayer(ui), 10f, 320f, UiAnchor.UPPER_LEFT);
        this.multiplayer.setVisible(false);
        this.singleplayer = ui.add(this, new LobbySingleplayer(ui, uiCatalog, textFormat), -300f, -10f, UiAnchor.UPPER_RIGHT);
        this.singleplayer.setVisible(false);
        this.saveGames = ui.add(this, new LobbySaveGames(ui), 0f, 0f, UiAnchor.UPPER_LEFT);
        this.saveGames.setVisible(false);
        this.bookmarks = ui.add(this, new LobbyBookmarks(ui), 0f, 1f, UiAnchor.UPPER_LEFT);
        this.bookmarks.setVisible(false);
        this.backButton = ui.button(this, newGameSkin, UiNewgameSkin.Styles.Button.GEN_BTN, 304f, -53f, UiAnchor.CENTER_DOWN);
        this.backLabel = ui.buttonLabel(this.backButton, "BACK", FontsSkin.Styles.Label.JOCKEY_16_DARK);
        this.playButton = ui.button(this, newGameSkin, UiNewgameSkin.Styles.Button.PLAY_BTN, 281f, -177f, UiAnchor.CENTER_DOWN);
        this.playLabel = ui.buttonLabel(this.playButton, "PLAY", FontsSkin.Styles.Label.JOCKEY_24_GLOW_BLUE);
        this.chatlog = ui.list(this, -506f, -163f, 722f, 120f, UiAnchor.CENTER_DOWN, 0f, 0f, 0f, LobbyChatText::new);
        this.multiplayer.setVisible(multiplayerMode);
        this.bookmarks.setVisible(true);
        this.lobbyChatEdit.setVisible(multiplayerMode);
        this.bookmarks.setBookmarks(List.of(bookmark));
        ui.onClick(this.newgameTab, () -> this.showBookmarks(true));
        ui.onClick(this.savedgameTab, () -> this.showBookmarks(false));
        ui.onClick(this.backButton, listener::onBackClicked);
        ui.onClick(this.playButton, listener::onPlayClicked);
        this.lobbyChatEdit.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode != Input.Keys.ENTER && keycode != Input.Keys.NUMPAD_ENTER) {
                    return false;
                }
                String message = Lobby.this.lobbyChatEdit.getText();
                if (!message.isBlank()) {
                    listener.onChatMessage(message);
                }
                Lobby.this.lobbyChatEdit.setText("");
                return true;
            }
        });
    }

    public void setHost(String playerName) {
        this.multiplayer.setHost(playerName, null);
    }

    public void showCountry(CountrySummary summary) {
        this.singleplayer.bind(summary);
        this.singleplayer.setVisible(true);
        if (this.multiplayerMode) {
            this.multiplayer.setHostFlag(this.singleplayer.flag(summary.countryIndex()));
        }
    }

    public void hideCountry() {
        this.singleplayer.setVisible(false);
    }

    public boolean isCountryShown() {
        return this.singleplayer.isVisible();
    }

    public void addChatLine(String text) {
        int index = this.chatlog.getRowCount();
        this.chatlog.setRowCount(index + 1);
        this.chatlog.row(index).setText(text);
        this.chatlog.layout();
        this.chatlog.setScrollPercentY(this.multiplayerMode ? 1f : 0f);
    }

    private void showBookmarks(boolean bookmarks) {
        this.bookmarks.setVisible(bookmarks);
        this.saveGames.setVisible(!bookmarks);
    }
}

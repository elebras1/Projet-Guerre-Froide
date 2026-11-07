package com.populaire.projetguerrefroide.ui.view.mainmenu;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ProjetGuerreFroide;
import com.populaire.projetguerrefroide.configuration.TutorialCategory;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiMainmenuSkin;
import com.populaire.projetguerrefroide.ui.view.settings.SettingsWindow;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;
import java.util.List;

public class MainMenu extends UiWindow {
    private final Image frontendBackdrop;
    private final Label versionLabel;
    private final MultiplayerMenu multiplayerMenu;
    private final MultiplayerConnectMenu multiplayerConnectMenu;
    private final MainMenuPanel mainmenuPanel;
    private final TutorialPanel tutorialPanel;
    private final SettingsWindow settingsWindow;
    private final CreditsWindow creditsWindow;
    private final NamingWindow namingWindow;
    private final MainMenuListener listener;
    private UiWindow current;

    public MainMenu(UiBuilder ui, MainMenuListener listener, List<TutorialCategory> tutorials) {
        super(ui);
        this.listener = listener;
        Skin mainMenuSkin = ui.skin(Skins.UI_MAINMENU);
        this.frontendBackdrop = ui.image(this, mainMenuSkin, UiMainmenuSkin.Drawables.FRONTEND_MAIN_BG, -960f, -540f, UiAnchor.CENTER);
        this.versionLabel = ui.label(this, "", FontsSkin.Styles.Label.ARIAL12, 5f, -25f, 400f, 50f, Align.topLeft, UiAnchor.LOWER_LEFT);
        this.multiplayerMenu = ui.add(this, new MultiplayerMenu(ui, this), -71f, -202f, UiAnchor.CENTER);
        this.multiplayerMenu.setVisible(false);
        this.multiplayerConnectMenu = ui.add(this, new MultiplayerConnectMenu(ui, this::showMultiplayerMenu), -210f, -250f, UiAnchor.CENTER);
        this.multiplayerConnectMenu.setVisible(false);
        this.mainmenuPanel = ui.add(this, new MainMenuPanel(ui, this), -512f, -360f, UiAnchor.CENTER);
        this.mainmenuPanel.setVisible(false);
        this.tutorialPanel = ui.add(this, new TutorialPanel(ui, tutorials, this::showPanel, listener::onTutorialPlayClicked), -394f, -270f, UiAnchor.CENTER);
        this.tutorialPanel.setVisible(false);
        this.versionLabel.setText(ProjetGuerreFroide.VERSION);
        this.settingsWindow = ui.add(this, new SettingsWindow(ui, this::applySettings, this::showPanel), -185f, -299f, UiAnchor.CENTER);
        this.settingsWindow.setVisible(false);
        this.creditsWindow = ui.add(this, new CreditsWindow(ui, this::showPanel), -71f, -202f, UiAnchor.CENTER);
        this.creditsWindow.setVisible(false);
        this.namingWindow = ui.add(this, new NamingWindow(ui), -45f, -100f, UiAnchor.CENTER);
        this.namingWindow.setVisible(false);
        this.mainmenuPanel.setVisible(true);
        this.current = this.mainmenuPanel;
    }

    @Override
    public void layout() {
        super.layout();
        this.frontendBackdrop.setBounds(0f, 0f, this.getWidth(), this.getHeight());
    }

    void onSinglePlayerClicked() {
        this.listener.onSinglePlayerClicked();
    }

    void onExitClicked() {
        this.listener.onExitClicked();
    }

    void showPanel() {
        this.show(this.mainmenuPanel);
    }

    void showMultiplayerMenu() {
        this.show(this.multiplayerMenu);
    }

    void showSettings() {
        this.settingsWindow.show(this.listener.onShowSettingsClicked());
        this.show(this.settingsWindow);
    }

    void showTutorial() {
        this.tutorialPanel.reset();
        this.show(this.tutorialPanel);
    }

    void showCredits() {
        this.creditsWindow.restart();
        this.show(this.creditsWindow);
    }

    void showHostNaming(String playerName) {
        this.namingWindow.open(this.ui.localise("HOST_TITLE"), this.ui.localise("FE_MY_GAME"), "FE_HOST",
            gameName -> this.listener.onHostClicked(gameName, playerName), this::showMultiplayerMenu);
        this.show(this.namingWindow);
    }

    void showJoinNaming(String playerName) {
        this.namingWindow.open(this.ui.localise("JOIN_IP_TITLE"), "", "FE_JOIN_INTERNET_GAME",
            address -> this.listener.onJoinInternetClicked(address, playerName), this::showMultiplayerMenu);
        this.show(this.namingWindow);
    }

    void onJoinLanClicked(String playerName) {
        this.listener.onJoinLanClicked(playerName);
    }

    void onScanClicked() {
        this.listener.onScanClicked();
    }

    void onMetaserverClicked() {
        this.listener.onMetaserverClicked();
    }

    public void showConnectionLog(String... keys) {
        this.multiplayerConnectMenu.setLog(keys);
        this.show(this.multiplayerConnectMenu);
    }

    public void setLocalGames(List<String> games) {
        this.multiplayerMenu.setGames(games);
    }

    public void showTutorialMessage(String key) {
        this.tutorialPanel.showMessage(key);
    }

    private void applySettings() {
        this.listener.onApplySettingsClicked(this.settingsWindow.getSettings());
    }

    private void show(UiWindow window) {
        this.current.setVisible(false);
        window.setVisible(true);
        this.current = window;
    }
}

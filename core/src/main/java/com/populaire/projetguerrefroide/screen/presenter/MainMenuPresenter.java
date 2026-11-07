package com.populaire.projetguerrefroide.screen.presenter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.populaire.projetguerrefroide.configuration.Settings;
import com.populaire.projetguerrefroide.screen.LobbySetup;
import com.populaire.projetguerrefroide.screen.ScreenManager;
import com.populaire.projetguerrefroide.ui.UiContext;
import com.populaire.projetguerrefroide.ui.UiResources;
import com.populaire.projetguerrefroide.ui.view.mainmenu.MainMenu;
import com.populaire.projetguerrefroide.ui.view.mainmenu.MainMenuListener;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;

public class MainMenuPresenter implements Presenter, MainMenuListener {
    private final ScreenManager screenManager;
    private final UiContext uiContext;
    private final UiResources uiResources;
    private MainMenu mainMenu;

    public MainMenuPresenter(ScreenManager screenManager, UiContext uiContext, UiResources uiResources) {
        this.screenManager = screenManager;
        this.uiContext = uiContext;
        this.uiResources = uiResources;
    }

    @Override
    public void initialize(Stage stage) {
        UiBuilder ui = new UiBuilder(this.uiContext);
        this.mainMenu = new MainMenu(ui, this, this.uiResources.loadTutorials(), this.uiResources.loadCredits());
        stage.addActor(this.mainMenu);
    }

    @Override
    public void update(float delta) {

    }

    @Override
    public void onSinglePlayerClicked() {
        this.screenManager.showLoadScreen(LobbySetup.singleplayer());
    }

    @Override
    public void onExitClicked() {
        Gdx.app.exit();
    }

    @Override
    public Settings onShowSettingsClicked() {
        return this.uiContext.getSettings().clone();
    }

    @Override
    public void onApplySettingsClicked(Settings settings) {
        this.uiContext.setSettings(settings.clone());
        this.uiResources.saveSettings(settings);
        settings.applyGraphicsSettings();
    }

    @Override
    public void onHostClicked(String gameName, String playerName) {
        this.screenManager.showLoadScreen(new LobbySetup(true, gameName, playerName));
    }

    @Override
    public void onJoinLanClicked(String playerName) {
        this.mainMenu.showConnectionLog("LOOKING_FOR_HOST", "FE_NETWORK_UNAVAILABLE");
    }

    @Override
    public void onJoinInternetClicked(String address, String playerName) {
        this.mainMenu.showConnectionLog("CONNECTING", "CONNECTION_FAILED", "FE_NETWORK_UNAVAILABLE");
    }

    @Override
    public void onScanClicked() {
        this.mainMenu.showConnectionLog("LOOKING_FOR_HOST", "FE_NETWORK_UNAVAILABLE");
    }

    @Override
    public void onMetaserverClicked() {
        this.mainMenu.showConnectionLog("CONNECTING", "FE_NETWORK_UNAVAILABLE");
    }

    @Override
    public void onTutorialPlayClicked(int category, int chapter) {
        this.mainMenu.showTutorialMessage("FE_TUTORIAL_UNAVAILABLE");
    }

    @Override
    public void dispose() {

    }
}

package com.populaire.projetguerrefroide.screen.presenter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.populaire.projetguerrefroide.configuration.Settings;
import com.populaire.projetguerrefroide.screen.GameFlowHandler;
import com.populaire.projetguerrefroide.screen.ScreenManager;
import com.populaire.projetguerrefroide.ui.UiContext;
import com.populaire.projetguerrefroide.ui.UiResources;
import com.populaire.projetguerrefroide.ui.view.dialog.DefaultConfirm;
import com.populaire.projetguerrefroide.ui.view.dialog.DefaultPopup;
import com.populaire.projetguerrefroide.ui.view.dialog.PopupListener;
import com.populaire.projetguerrefroide.ui.view.menubar.InGameMenu;
import com.populaire.projetguerrefroide.ui.view.menubar.InGameMenuListener;
import com.populaire.projetguerrefroide.ui.view.settings.IngameSettingsWindow;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class MainMenuInGamePresenter implements Presenter, InGameMenuListener, PopupListener {
    private final UiContext uiContext;
    private final UiResources uiResources;
    private final GameFlowHandler gameFlowHandler;
    private final ScreenManager screenManager;
    private UiBuilder ui;
    private Stage stage;
    private Table rootTable;
    private InGameMenu inGameMenu;
    private UiWindow menuLayer;
    private IngameSettingsWindow settingsWindow;
    private UiWindow popupLayer;
    private UiWindow currentPopup;
    private Runnable popupAction;

    public MainMenuInGamePresenter(UiContext uiContext, UiResources uiResources, GameFlowHandler gameFlowHandler, ScreenManager screenManager) {
        this.uiContext = uiContext;
        this.uiResources = uiResources;
        this.gameFlowHandler = gameFlowHandler;
        this.screenManager = screenManager;
    }

    @Override
    public void initialize(Stage stage) {
        this.stage = stage;
        this.ui = new UiBuilder(this.uiContext);
        this.rootTable = new Table();
        this.rootTable.setFillParent(true);
        stage.addActor(this.rootTable);

        this.menuLayer = new UiWindow(this.ui);
        this.menuLayer.setSize(stage.getWidth(), stage.getHeight());
        this.inGameMenu = this.ui.add(this.menuLayer, new InGameMenu(this.ui, this), -256f, -206f, UiAnchor.CENTER);
        this.settingsWindow = this.ui.add(this.menuLayer, new IngameSettingsWindow(this.ui, this::applySettings, this::closeSettings), -180f, -267f, UiAnchor.CENTER);
        this.settingsWindow.setVisible(false);
        this.menuLayer.setVisible(false);
        this.rootTable.addActor(this.menuLayer);

        this.popupLayer = new UiWindow(this.ui);
        this.popupLayer.setSize(stage.getWidth(), stage.getHeight());
        this.popupLayer.setTouchable(Touchable.enabled);
        this.popupLayer.setVisible(false);
        this.rootTable.addActor(this.popupLayer);
        this.syncLayers();
    }

    @Override
    public void update(float delta) {
        this.syncLayers();
    }

    private void syncLayers() {
        float width = this.stage.getWidth();
        float height = this.stage.getHeight();
        if (this.menuLayer.getWidth() != width || this.menuLayer.getHeight() != height) {
            this.menuLayer.setSize(width, height);
            this.menuLayer.invalidate();
        }
        this.popupLayer.setSize(width, height);
    }

    public void show() {
        this.menuLayer.setVisible(true);
        this.settingsWindow.setVisible(false);
        this.inGameMenu.setVisible(true);
        this.inGameMenu.showPanel();
        this.rootTable.toFront();
        this.gameFlowHandler.setInputEnabled(false);
    }

    public void hide() {
        this.menuLayer.setVisible(false);
        this.stage.unfocus(this.menuLayer);
        this.hidePopup();
        this.gameFlowHandler.setInputEnabled(true);
    }

    public void close() {
        this.hide();
        this.gameFlowHandler.resume();
    }

    @Override
    public void onSaveClicked(String fileName) {
        this.showPopup(new DefaultPopup(this.ui, false, "SAVE_GAME", "SAVE_UNAVAILABLE", null, null, this), null);
    }

    @Override
    public void onGameOptionsClicked() {
        this.settingsWindow.show(this.uiContext.getSettings().clone());
        this.inGameMenu.setVisible(false);
        this.settingsWindow.setVisible(true);
    }

    @Override
    public void onResignClicked() {
        this.showConfirm("RESIGN_TITLE", "RESIGN_DESC", () -> {
            this.hide();
            this.gameFlowHandler.resume();
            this.screenManager.showMainMenuScreen();
        });
    }

    @Override
    public void onQuitClicked() {
        this.showConfirm("QUIT_TITLE", "QUIT_DESC", () -> Gdx.app.exit());
    }

    @Override
    public void onCloseClicked() {
        this.close();
    }

    private void showConfirm(String titleKey, String descriptionKey, Runnable action) {
        this.showPopup(new DefaultConfirm(this.ui, titleKey, descriptionKey, null, null, this), action);
    }

    private void showPopup(UiWindow popup, Runnable action) {
        this.hidePopup();
        this.currentPopup = popup;
        this.popupAction = action;
        this.popupLayer.setPosition(0f, 0f);
        popup.setPosition((this.popupLayer.getWidth() - popup.getWidth()) / 2f, (this.popupLayer.getHeight() - popup.getHeight()) / 2f);
        this.popupLayer.addActor(popup);
        this.popupLayer.setVisible(true);
        this.popupLayer.toFront();
    }

    private void hidePopup() {
        if (this.currentPopup != null) {
            this.currentPopup.remove();
            this.currentPopup = null;
        }
        this.popupAction = null;
        this.popupLayer.setVisible(false);
    }

    @Override
    public void onOkClicked() {
        Runnable action = this.popupAction;
        this.hidePopup();
        if (action != null) {
            action.run();
        }
    }

    @Override
    public void onCancelClicked() {
        this.hidePopup();
    }

    private void applySettings() {
        Settings settings = this.settingsWindow.getSettings();
        this.uiContext.setSettings(settings.clone());
        this.uiResources.saveSettings(settings);
    }

    private void closeSettings() {
        this.settingsWindow.setVisible(false);
        this.inGameMenu.setVisible(true);
        this.inGameMenu.showPanel();
    }

    @Override
    public void dispose() {

    }
}

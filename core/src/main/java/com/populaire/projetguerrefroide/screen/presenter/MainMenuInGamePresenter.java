package com.populaire.projetguerrefroide.screen.presenter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.populaire.projetguerrefroide.configuration.Settings;
import com.populaire.projetguerrefroide.screen.GameFlowHandler;
import com.populaire.projetguerrefroide.screen.listener.MainMenuInGameListener;
import com.populaire.projetguerrefroide.service.ConfigurationService;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.ui.view.MainMenuInGame;
import com.populaire.projetguerrefroide.ui.view.Popup;
import com.populaire.projetguerrefroide.ui.view.PopupListener;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

public class MainMenuInGamePresenter implements Presenter, MainMenuInGameListener {
    private final GameContext gameContext;
    private final ConfigurationService configurationService;
    private final GameFlowHandler gameFlowHandler;
    private final WidgetFactory widgetFactory;
    private final Skin skinMenu;
    private final Skin skinUi;
    private final Skin skinScrollbars;
    private final Skin skinPopup;
    private final Skin skinFlags;
    private MainMenuInGame mainMenuInGame;
    private Table rootTable;
    private Popup currentPopup;
    private Stage stage;

    public MainMenuInGamePresenter(GameContext gameContext, ConfigurationService configurationService, GameFlowHandler gameFlowHandler, WidgetFactory widgetFactory, Skin skinMenu, Skin skinUi, Skin skinScrollbars, Skin skinPopup, Skin skinFlags) {
        this.gameContext = gameContext;
        this.configurationService = configurationService;
        this.gameFlowHandler = gameFlowHandler;
        this.widgetFactory = widgetFactory;
        this.skinMenu = skinMenu;
        this.skinUi = skinUi;
        this.skinScrollbars = skinScrollbars;
        this.skinPopup = skinPopup;
        this.skinFlags = skinFlags;
    }

    @Override
    public void initialize(Stage stage) {
        this.stage = stage;
        this.mainMenuInGame = new MainMenuInGame(widgetFactory, skinMenu, skinUi, skinScrollbars, gameContext.getLabelStylePool(), gameContext.getLocalisation(), this);
        this.mainMenuInGame.setVisible(false);

        this.rootTable = new Table();
        this.rootTable.setFillParent(true);
        this.rootTable.add(this.mainMenuInGame).center();
        stage.addActor(this.rootTable);
    }

    @Override
    public void refresh() {

    }

    @Override
    public void update(float delta) {

    }

    public void show() {
        this.mainMenuInGame.setVisible(true);
        this.rootTable.toFront();
        this.gameFlowHandler.setInputEnabled(false);
    }

    public void hide() {
        this.mainMenuInGame.setVisible(false);
        this.gameFlowHandler.setInputEnabled(true);
    }

    @Override
    public void onCloseMainMenuInGameClicked() {
        this.hide();
        this.gameFlowHandler.resume();
    }

    @Override
    public void onQuitClicked(PopupListener listener) {
        this.currentPopup = new Popup(widgetFactory, skinPopup, skinUi, skinFlags, gameContext.getLabelStylePool(), gameContext.getLocalisation(), "QUIT_TITLE", "QUIT_DESC", true, false, listener);
        Table table = new Table();
        table.setFillParent(true);
        table.add(this.currentPopup).center();
        this.stage.addActor(table);
        this.mainMenuInGame.setTouchable(Touchable.disabled);
    }

    @Override
    public void onOkPopupClicked() {
        Gdx.app.exit();
    }

    @Override
    public void onCancelPopupClicked() {
        this.currentPopup.remove();
        this.currentPopup = null;
        this.mainMenuInGame.setTouchable(Touchable.enabled);
    }

    @Override public Settings onShowSettingsClicked() {
        return gameContext.getSettings().clone();
    }

    @Override public void onApplySettingsClicked(Settings settings) {
        this.gameContext.setSettings(settings);
        this.configurationService.saveSettings(settings);
    }

    @Override public void dispose() {

    }
}

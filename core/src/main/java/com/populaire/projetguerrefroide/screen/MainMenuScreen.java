package com.populaire.projetguerrefroide.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.monstrous.gdx.webgpu.graphics.utils.WgScreenUtils;
import com.monstrous.gdx.webgpu.scene2d.WgStage;
import com.populaire.projetguerrefroide.adapter.graphics.WgScreenViewport;
import com.populaire.projetguerrefroide.service.ConfigurationService;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.ui.view.MainMenu;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

public class MainMenuScreen implements Screen, MainMenuListener {
    private final Stage stage;
    private final ScreenManager screenManager;
    private final GameContext gameContext;

    public MainMenuScreen(ScreenManager screenManager, GameContext gameContext, ConfigurationService configurationService) {
        this.stage = new WgStage(new WgScreenViewport());
        this.screenManager = screenManager;
        this.gameContext = gameContext;
        configurationService.loadMainMenuLocalisation(gameContext);
        gameContext.getSettings().applyGraphicsSettings();
        Gdx.input.setInputProcessor(this.stage);
        this.initializeUi(gameContext);
    }

    public void initializeUi(GameContext gameContext) {
        AssetManager assetManager = gameContext.getAssetManager();
        Skin skin = assetManager.get("ui/mainmenu/mainmenu_skin.json");
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.setBackground(skin.getDrawable("frontend_main_bg"));
        WidgetFactory widgetFactory = new WidgetFactory();
        MainMenu menu = new MainMenu(widgetFactory, skin, gameContext.getLabelStylePool(), gameContext.getLocalisation(), this);
        rootTable.add(menu).center().padLeft(menu.getWidth() / 3);
        this.stage.addActor(rootTable);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        WgScreenUtils.clear(1, 1, 1, 1);

        this.stage.act();
        this.stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        this.stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        this.stage.dispose();
    }

    @Override
    public void onSinglePlayerClicked() {
        this.screenManager.showLoadScreen();
    }

    @Override
    public void onMultiplayerClicked() {

    }

    @Override
    public void onExitClicked() {
        Gdx.app.exit();
    }
}

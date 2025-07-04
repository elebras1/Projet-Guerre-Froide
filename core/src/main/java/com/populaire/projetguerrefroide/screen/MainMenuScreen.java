package com.populaire.projetguerrefroide.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.populaire.projetguerrefroide.service.ConfigurationService;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.ui.view.MainMenu;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

public class MainMenuScreen implements Screen, MainMenuListener {
    private final Stage stage;
    private final ScreenManager screenManager;

    public MainMenuScreen(ScreenManager screenManager, GameContext gameContext, ConfigurationService configurationService) {
        this.stage = new Stage(new ScreenViewport());
        this.screenManager = screenManager;
        gameContext.getSettings().applyGraphicsSettings();
        Gdx.input.setInputProcessor(this.stage);
        AssetManager assetManager = gameContext.getAssetManager();
        assetManager.load("ui/mainmenu/mainmenu_skin.json", Skin.class);
        assetManager.finishLoading();
        Skin skin = assetManager.get("ui/mainmenu/mainmenu_skin.json");
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.setBackground(skin.getDrawable("frontend_main_bg"));
        configurationService.loadMainMenuLocalisation(gameContext);
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
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL32.GL_COLOR_BUFFER_BIT);

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

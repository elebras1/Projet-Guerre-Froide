package com.populaire.projetguerrefroide.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.monstrous.gdx.webgpu.graphics.utils.WgScreenUtils;
import com.monstrous.gdx.webgpu.scene2d.WgStage;
import com.populaire.projetguerrefroide.adapter.graphics.WgScreenViewport;
import com.populaire.projetguerrefroide.screen.presenter.Presenter;
import com.populaire.projetguerrefroide.screen.presenter.MainMenuPresenter;
import com.populaire.projetguerrefroide.service.ConfigurationService;
import com.populaire.projetguerrefroide.service.GameContext;

public class MainMenuScreen implements Screen {
    private final Stage stage;
    private final ScreenManager screenManager;
    private final GameContext gameContext;
    private final Presenter mainMenuPresenter;

    public MainMenuScreen(ScreenManager screenManager, GameContext gameContext, ConfigurationService configurationService) {
        this.stage = new WgStage(new WgScreenViewport());
        this.screenManager = screenManager;
        this.gameContext = gameContext;
        configurationService.loadMainMenuLocalisation(gameContext);
        gameContext.getSettings().applyGraphicsSettings();
        Gdx.input.setInputProcessor(this.stage);
        this.mainMenuPresenter = new MainMenuPresenter(this.screenManager, this.gameContext);
    }

    @Override
    public void show() {
        this.mainMenuPresenter.initialize(this.stage);
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
        this.mainMenuPresenter.dispose();
        this.stage.dispose();
    }
}

package com.populaire.projetguerrefroide.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.monstrous.gdx.webgpu.scene2d.WgStage;
import com.populaire.projetguerrefroide.screen.presenter.LoadingPresenter;
import com.populaire.projetguerrefroide.service.ConfigurationService;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.service.WorldService;

import static com.populaire.projetguerrefroide.screen.LoadingStep.*;

public class LoadScreen implements Screen {
    private final Stage stage;
    private final ScreenManager screenManager;
    private final GameContext gameContext;
    private final WorldService worldService;
    private final ConfigurationService configurationService;
    private final LoadingPresenter loadingPresenter;
    private LoadingStep loadingStep;

    public LoadScreen(ScreenManager screenManager, GameContext gameContext, ConfigurationService configurationService) {
        this.screenManager = screenManager;
        this.gameContext = gameContext;
        this.worldService = new WorldService(gameContext);
        this.configurationService = configurationService;
        this.loadingPresenter = new LoadingPresenter(this.gameContext);
        this.gameContext.getCursorManager().animatedCursor("busy");

        AssetManager assetManager = this.gameContext.getAssetManager();
        assetManager.load("loadingscreens/loadingscreens_skin.json", Skin.class);
        this.stage = new WgStage();
        Gdx.input.setInputProcessor(this.stage);
        this.loadingStep = LOADING_ASSETS;
    }

    @Override
    public void show() {
        AssetManager assetManager = this.gameContext.getAssetManager();
        this.loadingPresenter.initialize(this.stage);
        assetManager.finishLoading();

        this.configurationService.loadGameAssets(assetManager);
    }

    @Override
    public void render(float delta) {
        switch (this.loadingStep) {
            case LOADING_ASSETS -> {
                this.stage.act();
                this.stage.draw();
                this.gameContext.getCursorManager().update(delta);
                if (this.gameContext.getAssetManager().update()) {
                    this.loadingStep = CREATING_WORLD;
                }
            }
            case CREATING_WORLD -> {
                this.worldService.createWorld();
                this.loadingStep = FINISHED;
            }
            case FINISHED -> {
                this.gameContext.getCursorManager().update(delta);
                this.gameContext.getSettings().applyGraphicsSettings();
                this.gameContext.getAssetManager().finishLoading();
                this.screenManager.showNewGameScreen(this.worldService);
            }
        }
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
        if (this.gameContext.getAssetManager().isLoaded("loadingscreens/loadingscreens_skin.json")) {
            this.gameContext.getAssetManager().unload("loadingscreens/loadingscreens_skin.json");
        }
    }
}

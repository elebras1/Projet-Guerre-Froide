package com.populaire.projetguerrefroide.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.monstrous.gdx.webgpu.scene2d.WgStage;
import com.populaire.projetguerrefroide.screen.presenter.LoadingPresenter;
import com.populaire.projetguerrefroide.ui.UiResources;
import com.populaire.projetguerrefroide.ui.UiContext;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;

import static com.populaire.projetguerrefroide.screen.LoadingStep.*;

public class LoadScreen implements Screen {
    private final Stage stage;
    private final ScreenManager screenManager;
    private final UiContext uiContext;
    private final UiResources uiResources;
    private final GameSession session;
    private final LoadingPresenter loadingPresenter;
    private LoadingStep loadingStep;

    public LoadScreen(ScreenManager screenManager, UiContext uiContext, UiResources uiResources, GameSession session) {
        this.screenManager = screenManager;
        this.uiContext = uiContext;
        this.uiResources = uiResources;
        this.session = session;
        this.loadingPresenter = new LoadingPresenter(this.uiContext);

        AssetManager assetManager = this.uiContext.getAssetManager();
        assetManager.load(Skins.LOADINGSCREENS, Skin.class);
        this.stage = new WgStage();
        Gdx.input.setInputProcessor(this.stage);
        this.loadingStep = LOADING_ASSETS;
    }

    @Override
    public void show() {
        AssetManager assetManager = this.uiContext.getAssetManager();
        this.loadingPresenter.initialize(this.stage);
        assetManager.finishLoading();
        this.session.channel().start();
        this.uiResources.loadGameAssets(assetManager);
    }

    @Override
    public void render(float delta) {
        this.uiContext.getCursorManager().update(delta);
        switch (this.loadingStep) {
            case LOADING_ASSETS -> {
                if (this.uiContext.getAssetManager().update()) {
                    this.loadingStep = CREATING_WORLD;
                }
            }
            case CREATING_WORLD -> {
                if (this.session.prepare()) {
                    this.loadingStep = FINISHED;
                }
            }
            case FINISHED -> {
                this.uiContext.getSettings().applyGraphicsSettings();
                this.uiContext.getAssetManager().finishLoading();
                this.screenManager.showNewGameScreen();
                return;
            }
        }
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
        if (this.uiContext.getAssetManager().isLoaded(Skins.LOADINGSCREENS)) {
            this.uiContext.getAssetManager().unload(Skins.LOADINGSCREENS);
        }
    }
}

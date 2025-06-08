package com.populaire.projetguerrefroide.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.service.ConfigurationService;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.service.WorldService;
import com.populaire.projetguerrefroide.util.Logging;

import java.util.List;
import java.util.Random;

public class LoadScreen implements Screen {
    private final Stage stage;
    private final Skin skin;
    private final ScreenManager screenManager;
    private final GameContext gameContext;
    private final WorldService worldService;
    private final ConfigurationService configurationService;

    public LoadScreen(ScreenManager screenManager, GameContext gameContext, ConfigurationService configurationService) {
        this.screenManager = screenManager;
        this.gameContext = gameContext;
        this.worldService = new WorldService();
        this.configurationService = configurationService;
        this.gameContext.getCursorManager().animatedCursor("busy");
        AssetManager assetManager = this.gameContext.getAssetManager();
        assetManager.load("loadingscreens/loadingscreens_skin.json", Skin.class);
        this.stage = new Stage();
        Gdx.input.setInputProcessor(this.stage);
        List<String> loadingImageNames = ObjectList.with("load_1", "load_2", "load_3", "load_4", "load_5", "load_6", "load_7", "load_8", "load_9", "load_10", "load_11", "load_12");
        Random random = new Random();
        Table rootTable = new Table();
        assetManager.finishLoading();
        this.skin = assetManager.get("loadingscreens/loadingscreens_skin.json", Skin.class);
        rootTable.setFillParent(true);
        Drawable background = this.skin.getDrawable(loadingImageNames.get(random.nextInt(loadingImageNames.size())));
        rootTable.setBackground(background);
        this.stage.addActor(rootTable);
        Gdx.graphics.setForegroundFPS(1);
    }

    @Override
    public void show() {
        this.configurationService.loadGameAssets(gameContext.getAssetManager());
        this.worldService.getAsyncExecutor().submit(() -> {
            long startTime = System.currentTimeMillis();
            this.worldService.createWorld(this.gameContext);

            Gdx.app.postRunnable(() -> {
                this.gameContext.getSettings().applyGraphicsSettings();
                this.screenManager.showNewGameScreen(this.worldService);
            });

            long endTime = System.currentTimeMillis();
            Logging.getLogger("LoadScreen").info("World load: " + (endTime - startTime) + "ms");

            return null;
        });
        this.gameContext.getAssetManager().finishLoading();
    }

    @Override
    public void render(float delta) {
        this.stage.act();
        this.stage.draw();

        this.gameContext.getCursorManager().update(delta);
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
        this.skin.dispose();
        this.gameContext.getAssetManager().unload("loadingscreens/loadingscreens_skin.json");

    }
}

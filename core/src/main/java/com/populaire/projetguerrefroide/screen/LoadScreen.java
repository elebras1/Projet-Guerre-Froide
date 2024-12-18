package com.populaire.projetguerrefroide.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.populaire.projetguerrefroide.service.WorldService;
import com.populaire.projetguerrefroide.ui.CursorManager;
import com.populaire.projetguerrefroide.util.Logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class LoadScreen implements Screen {
    private final Stage stage;
    private final Skin skin;
    private final ScreenManager screenManager;
    private final AssetManager assetManager;
    private final CursorManager cursorManager;

    public LoadScreen(ScreenManager screenManager, AssetManager assetManager, CursorManager cursorManager) {
        this.screenManager = screenManager;
        this.assetManager = assetManager;
        this.assetManager.load("loadingscreens/loadingscreens_skin.json", Skin.class);
        this.stage = new Stage();
        Gdx.input.setInputProcessor(this.stage);
        List<String> loadingImageNames = new ArrayList<>(Arrays.asList("load_1", "load_2", "load_3", "load_4", "load_5", "load_6", "load_7", "load_8", "load_9", "load_10", "load_11", "load_12"));
        this.cursorManager = cursorManager;
        this.cursorManager.animatedCursor("busy");
        Random random = new Random();
        Table rootTable = new Table();
        this.assetManager.finishLoading();
        this.skin = this.assetManager.get("loadingscreens/loadingscreens_skin.json", Skin.class);
        rootTable.setFillParent(true);
        Drawable background = this.skin.getDrawable(loadingImageNames.get(random.nextInt(loadingImageNames.size())));
        rootTable.setBackground(background);
        this.stage.addActor(rootTable);
        Gdx.graphics.setForegroundFPS(1);
    }

    @Override
    public void show() {
        CompletableFuture.runAsync(() -> {
            long startTime = System.currentTimeMillis();
            WorldService worldService = new WorldService();
            worldService.createWorldAsync();
            Gdx.app.postRunnable(() -> {
                Gdx.graphics.setForegroundFPS(1000);
                this.screenManager.showNewGameScreen(worldService);
            });
            long endTime = System.currentTimeMillis();
            Logging.getLogger("LoadScreen").info("World load: " + (endTime - startTime) + "ms");
        });
    }

    @Override
    public void render(float delta) {
        this.stage.act();
        this.stage.draw();

        this.cursorManager.update(delta);
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
        this.assetManager.unload("loadingscreens/loadingscreens_skin.json");

    }
}


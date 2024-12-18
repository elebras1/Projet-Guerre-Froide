package com.populaire.projetguerrefroide.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL32;
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
    private Skin skin;
    private final List<String> loadingImageNames;
    private final ScreenManager screenManager;
    private final AssetManager assetManager;
    private final CursorManager cursorManager;

    public LoadScreen(ScreenManager screenManager, AssetManager assetManager, CursorManager cursorManager) {
        this.screenManager = screenManager;
        this.assetManager = assetManager;
        this.assetManager.load("loadingscreens/loadingscreens_skin.json", Skin.class);
        this.stage = new Stage();
        Gdx.input.setInputProcessor(this.stage);
        this.loadingImageNames = new ArrayList<>(Arrays.asList("load_1", "load_2", "load_3", "load_4", "load_5", "load_6", "load_7", "load_8", "load_9", "load_10", "load_11", "load_12"));
        this.cursorManager = cursorManager;
        this.cursorManager.animatedCursor("busy");
        Random random = new Random();
        Table rootTable = new Table();
        this.assetManager.finishLoading();
        this.skin = this.assetManager.get("loadingscreens/loadingscreens_skin.json", Skin.class);
        rootTable.setFillParent(true);
        Drawable background = this.skin.getDrawable(this.loadingImageNames.get(random.nextInt(this.loadingImageNames.size())));
        rootTable.setBackground(background);
        this.stage.addActor(rootTable);
    }

    @Override
    public void show() {
        CompletableFuture.runAsync(() -> {
            long startTime = System.currentTimeMillis();
            WorldService worldService = new WorldService();
            worldService.createWorldAsync();
            Gdx.app.postRunnable(() -> {
                this.screenManager.showNewGameScreen(worldService);
            });
            long endTime = System.currentTimeMillis();
            Logging.getLogger("LoadScreen").info("World load: " + (endTime - startTime) + "ms");
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL32.GL_COLOR_BUFFER_BIT);

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


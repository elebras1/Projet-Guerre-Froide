package com.populaire.projetguerrefroide.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.populaire.projetguerrefroide.ui.CursorManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class LoadScreen implements Screen {
    private final Stage stage;
    private Skin skin;
    private final List<String> loadingImageNames;
    private final CursorManager cursorManager;

    public LoadScreen(ScreenManager screenManager, AssetManager assetManager, CursorManager cursorManager) {
        this.stage = new Stage();
        this.loadingImageNames = new ArrayList<>(Arrays.asList("load_1", "load_2", "load_3", "load_4", "load_5", "load_6", "load_7", "load_8", "load_9"));
        Gdx.input.setInputProcessor(this.stage);
        this.skin = new Skin(Gdx.files.internal("loadingscreens/loadingscreens_skin.json"));
        Random random = new Random();
        Drawable background = skin.getDrawable(this.loadingImageNames.get(random.nextInt(this.loadingImageNames.size())));
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.setBackground(background);
        this.stage.addActor(rootTable);
        this.cursorManager = cursorManager;
        this.cursorManager.animatedCursor("busy");
        CompletableFuture.runAsync(() -> {
            Gdx.app.postRunnable(screenManager::showNewGameScreen);
        });
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
    }
}


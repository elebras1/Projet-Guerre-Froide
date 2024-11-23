package com.populaire.projetguerrefroide.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.populaire.projetguerrefroide.ui.CursorManager;
import com.populaire.projetguerrefroide.ui.MainMenu;
import com.populaire.projetguerrefroide.data.DataManager;


public class MainMenuScreen implements Screen, MainMenuListener {
    private final Stage stage;
    private final ScreenManager screenManager;

    public MainMenuScreen(ScreenManager screenManager, AssetManager assetManager, CursorManager cursorManager) {
        this.stage = new Stage(new ScreenViewport());
        this.screenManager = screenManager;
        Gdx.input.setInputProcessor(this.stage);
        DataManager dataManager = new DataManager();
        assetManager.load("ui/mainmenu/mainmenu_skin.json", Skin.class);
        assetManager.finishLoading();
        Skin skin = assetManager.get("ui/mainmenu/mainmenu_skin.json");
        Skin skinFonts = assetManager.get("ui/fonts/fonts_skin.json");
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.setBackground(skin.getDrawable("frontend_main_bg"));
        MainMenu menu = new MainMenu(skin, skinFonts, dataManager.readMainMenuLocalisationCsv(), this);
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

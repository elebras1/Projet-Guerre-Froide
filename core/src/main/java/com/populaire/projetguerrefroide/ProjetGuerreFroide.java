package com.populaire.projetguerrefroide;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.populaire.projetguerrefroide.screens.ScreenManager;
import com.populaire.projetguerrefroide.ui.CursorManager;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class ProjetGuerreFroide extends Game {
    public static final int WORLD_WIDTH = 5616;
    public static final int WORLD_HEIGHT = 2160;

    @Override
    public void create() {
        AssetManager assetManager = new AssetManager();
        CursorManager cursorManager = new CursorManager();
        cursorManager.defaultCursor();
        this.loadAssets(assetManager);
        ScreenManager screenManager = new ScreenManager(this, assetManager, cursorManager);
        screenManager.showMainMenuScreen();
    }

    private void loadAssets(AssetManager assetManager) {
        assetManager.load("ui/ui_skin.json", Skin.class);
        assetManager.load("ui/fonts/fonts_skin.json", Skin.class);
        assetManager.load("ui/scrollbars/scrollbars_skin.json", Skin.class);
        assetManager.finishLoading();
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}

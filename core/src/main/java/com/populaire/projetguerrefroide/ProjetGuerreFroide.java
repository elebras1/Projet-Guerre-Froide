package com.populaire.projetguerrefroide;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.populaire.projetguerrefroide.screen.ScreenManager;
import com.populaire.projetguerrefroide.service.ConfigurationService;
import com.populaire.projetguerrefroide.service.GameContext;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class ProjetGuerreFroide extends Game {
    public static final int WORLD_WIDTH = 5616;
    public static final int WORLD_HEIGHT = 2160;
    private final ConfigurationService configurationService;

    public ProjetGuerreFroide() {
        this.configurationService = new ConfigurationService();
    }

    @Override
    public void create() {
        GameContext gameContext = this.configurationService.getGameContext();
        ScreenManager screenManager = new ScreenManager(this, gameContext, configurationService);
        this.loadAssets(gameContext.getAssetManager());
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

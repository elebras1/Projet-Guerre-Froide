package com.populaire.projetguerrefroide;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.elebras1.flecs.Flecs;
import com.populaire.projetguerrefroide.component.Ideology;
import com.populaire.projetguerrefroide.component.Minister;
import com.populaire.projetguerrefroide.component.MinisterType;
import com.populaire.projetguerrefroide.component.Modifier;
import com.populaire.projetguerrefroide.screen.ScreenManager;
import com.populaire.projetguerrefroide.service.ConfigurationService;
import com.populaire.projetguerrefroide.service.GameContext;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class ProjetGuerreFroide extends Game {
    public static final int WORLD_WIDTH = 5616;
    public static final int WORLD_HEIGHT = 2160;
    private final ConfigurationService configurationService;
    private final Flecs ecsWorld;

    public ProjetGuerreFroide() {
        this.configurationService = new ConfigurationService();
        this.ecsWorld = new Flecs();
        this.ecsWorld.component(Modifier.class);
        this.ecsWorld.component(Minister.class);
        this.ecsWorld.component(MinisterType.class);
        this.ecsWorld.component(Ideology.class);
    }

    @Override
    public void create() {
        GameContext gameContext = this.configurationService.getGameContext(this.ecsWorld);
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
        this.ecsWorld.close();
    }
}

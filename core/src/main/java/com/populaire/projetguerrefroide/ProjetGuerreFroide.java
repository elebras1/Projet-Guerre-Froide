package com.populaire.projetguerrefroide;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.configuration.Settings;
import com.populaire.projetguerrefroide.screen.ScreenManager;
import com.populaire.projetguerrefroide.service.ConfigurationService;
import com.populaire.projetguerrefroide.service.GameContext;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class ProjetGuerreFroide extends Game {
    public static final int WORLD_WIDTH = 5616;
    public static final int WORLD_HEIGHT = 2160;
    private final ConfigurationService configurationService;
    private ScreenManager screenManager;
    private GameContext gameContext;
    private final World ecsWorld;

    public ProjetGuerreFroide() {
        this.configurationService = new ConfigurationService();
        this.ecsWorld = new World();
    }

    @Override
    public void create() {
        this.registerComponents();
        this.gameContext = this.configurationService.getGameContext(this.ecsWorld);
        this.screenManager = new ScreenManager(this, this.gameContext, this.configurationService);
        this.loadAssets(this.gameContext.getAssetManager());
        this.screenManager.showMainMenuScreen();
        this.ecsDebug(gameContext);
    }

    private void registerComponents() {
        this.ecsWorld.component(Modifiers.class);
        this.ecsWorld.component(Minister.class);
        this.ecsWorld.component(Ideology.class);
        this.ecsWorld.component(Terrain.class);
        this.ecsWorld.component(ElectoralMechanism.class);
        this.ecsWorld.component(Leader.class);
        this.ecsWorld.component(EnactmentDuration.class);
        this.ecsWorld.component(Color.class);
        this.ecsWorld.component(Position.class);
        this.ecsWorld.component(Border.class);
        this.ecsWorld.component(DiplomaticRelation.class);
        this.ecsWorld.component(Adjacencies.class);
        this.ecsWorld.component(Country.class);
        this.ecsWorld.component(Province.class);
        this.ecsWorld.component(GeoHierarchy.class);
        this.ecsWorld.component(Law.class);
        this.ecsWorld.component(GovernmentPolicy.class);
    }

    private void loadAssets(AssetManager assetManager) {
        assetManager.load("ui/ui_skin.json", Skin.class);
        assetManager.load("ui/fonts/fonts_skin.json", Skin.class);
        assetManager.load("ui/scrollbars/scrollbars_skin.json", Skin.class);
        assetManager.finishLoading();
    }

    private void ecsDebug(GameContext gameContext) {
        Settings settings = gameContext.getSettings();
        if(settings.isDebugMode()) {
            this.ecsWorld.enableRest((short) 27750);
        }
    }

    @Override
    public void dispose() {
        this.ecsWorld.disableRest();
        this.ecsWorld.close();
        this.screenManager.dispose();
        this.gameContext.dispose();
        super.dispose();
    }
}

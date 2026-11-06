package com.populaire.projetguerrefroide;

import com.badlogic.gdx.Game;
import com.populaire.projetguerrefroide.screen.ScreenManager;
import com.populaire.projetguerrefroide.service.ConfigurationService;
import com.populaire.projetguerrefroide.simulation.SimulationRuntime;
import com.populaire.projetguerrefroide.ui.UiContext;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class ProjetGuerreFroide extends Game {
    public static final int WORLD_WIDTH = 5616;
    public static final int WORLD_HEIGHT = 2160;
    private final ConfigurationService configurationService;
    private ScreenManager screenManager;
    private UiContext uiContext;

    public ProjetGuerreFroide() {
        this.configurationService = new ConfigurationService();
    }

    @Override
    public void create() {
        this.uiContext = this.configurationService.getUiContext();
        this.screenManager = new ScreenManager(this, this.uiContext, this.configurationService, SimulationRuntime::threaded);
        this.screenManager.showMainMenuScreen();
    }

    @Override
    public void dispose() {
        this.screenManager.dispose();
        this.uiContext.dispose();
        super.dispose();
    }
}

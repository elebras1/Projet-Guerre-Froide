package com.populaire.projetguerrefroide.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.populaire.projetguerrefroide.service.ConfigurationService;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.service.WorldService;

public class ScreenManager {
    private final Game game;
    private final GameContext gameContext;
    private final ConfigurationService configurationService;

    public ScreenManager(Game game, GameContext gameContext, ConfigurationService configurationService) {
        this.game = game;
        this.gameContext = gameContext;
        this.configurationService = configurationService;
    }

    public void showMainMenuScreen() {
        this.showScreen(new MainMenuScreen(this, this.gameContext, this.configurationService));
    }

    public void showLoadScreen() {
        this.showScreen(new LoadScreen(this, this.gameContext, this.configurationService));
    }

    public void showNewGameScreen(WorldService worldService) {
        this.showScreen(new NewGameScreen(this, this.gameContext, worldService, this.configurationService));
    }

    public void showGameScreen(WorldService worldService) {
        this.showScreen(new GameScreen(this, this.gameContext, worldService, this.configurationService));
    }

    public void showScreen(Screen newScreen) {
        Screen screen = this.game.getScreen();
        this.game.setScreen(newScreen);
        if (screen != null) {
            screen.dispose();
        }
    }

    public void dispose() {
        this.game.dispose();
        this.gameContext.dispose();
    }
}

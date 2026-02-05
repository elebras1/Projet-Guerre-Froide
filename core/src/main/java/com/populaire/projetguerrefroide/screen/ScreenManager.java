package com.populaire.projetguerrefroide.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.Disposable;
import com.populaire.projetguerrefroide.command.CommandBus;
import com.populaire.projetguerrefroide.service.ConfigurationService;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.service.TimeService;
import com.populaire.projetguerrefroide.service.WorldService;

public class ScreenManager implements Disposable {
    private final Game game;
    private final GameContext gameContext;
    private final ConfigurationService configurationService;
    private final WorldService worldService;
    private final TimeService timeService;
    private final CommandBus commandBus;

    public ScreenManager(Game game, GameContext gameContext, ConfigurationService configurationService, WorldService worldService, TimeService timeService, CommandBus commandBus) {
        this.game = game;
        this.gameContext = gameContext;
        this.configurationService = configurationService;
        this.worldService = worldService;
        this.timeService = timeService;
        this.commandBus = commandBus;
    }

    public void showMainMenuScreen() {
        this.showScreen(new MainMenuScreen(this, this.gameContext, this.configurationService));
    }

    public void showLoadScreen() {
        this.showScreen(new LoadScreen(this, this.gameContext, this.configurationService, this.worldService));
    }

    public void showNewGameScreen() {
        this.showScreen(new NewGameScreen(this, this.gameContext, this.worldService, this.configurationService));
    }

    public void showGameScreen() {
        this.showScreen(new GameScreen(this, this.gameContext, this.worldService, this.timeService, this.configurationService, this.commandBus));
    }

    public void showScreen(Screen newScreen) {
        Screen screen = this.game.getScreen();
        this.game.setScreen(newScreen);
        if (screen != null) {
            screen.dispose();
        }
    }

    @Override
    public void dispose() {
    }
}

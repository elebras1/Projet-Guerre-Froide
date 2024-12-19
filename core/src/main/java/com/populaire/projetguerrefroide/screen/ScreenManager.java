package com.populaire.projetguerrefroide.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.service.WorldService;

public class ScreenManager {
    private final Game game;
    private final GameContext gameContext;

    public ScreenManager(Game game, GameContext gameContext) {
        this.game = game;
        this.gameContext = gameContext;
    }

    public void showMainMenuScreen() {
        this.showScreen(new MainMenuScreen(this, this.gameContext));
    }

    public void showLoadScreen() {
        this.showScreen(new LoadScreen(this, this.gameContext));
    }

    public void showNewGameScreen(WorldService worldService) {
        this.showScreen(new NewGameScreen(this, this.gameContext, worldService));
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

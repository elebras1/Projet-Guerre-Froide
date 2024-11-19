package com.populaire.projetguerrefroide.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.populaire.projetguerrefroide.ui.CursorManager;

public class ScreenManager {
    private Game game;
    private AssetManager assetManager;
    private CursorManager cursorManager;

    public ScreenManager(Game game, AssetManager assetManager, CursorManager cursorManager) {
        this.game = game;
        this.assetManager = assetManager;
        this.cursorManager = cursorManager;
    }

    public void showMainMenuScreen() {
        this.showScreen(new MainMenuScreen(this, assetManager, cursorManager));
    }

    public void showLoadScreen() {
        this.showScreen(new LoadScreen(this, assetManager, cursorManager));
    }

    public void showNewGameScreen() {
        this.showScreen(new NewGameScreen(this, assetManager, cursorManager));
    }

    public void showScreen(Screen newScreen) {
        Screen screen = this.game.getScreen();
        this.game.setScreen(newScreen);
        if (screen != null) {
            screen.dispose();
        }
    }

    public void dispose() {
        this.assetManager.dispose();
        this.cursorManager.dispose();
    }
}

package com.populaire.projetguerrefroide.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.Disposable;
import com.populaire.projetguerrefroide.protocol.SimulationChannel;
import com.populaire.projetguerrefroide.protocol.SimulationFactory;
import com.populaire.projetguerrefroide.ui.UiResources;
import com.populaire.projetguerrefroide.ui.UiContext;

public class ScreenManager implements Disposable {
    private final Game game;
    private final UiContext uiContext;
    private final UiResources uiResources;
    private final SimulationFactory simulationFactory;
    private GameSession session;

    public ScreenManager(Game game, UiContext uiContext, UiResources uiResources, SimulationFactory simulationFactory) {
        this.game = game;
        this.uiContext = uiContext;
        this.uiResources = uiResources;
        this.simulationFactory = simulationFactory;
    }

    public void showMainMenuScreen() {
        this.showScreen(new MainMenuScreen(this, this.uiContext, this.uiResources));
        this.closeSession();
    }

    public void showLoadScreen() {
        this.closeSession();
        SimulationChannel channel = this.simulationFactory.create(this.uiContext.getBookmark().date(), this.uiContext.getSettings().isDebugMode());
        this.session = new GameSession(channel, this.uiContext);
        this.showScreen(new LoadScreen(this, this.uiContext, this.uiResources, this.session));
    }

    public void showNewGameScreen() {
        this.showScreen(new NewGameScreen(this, this.uiContext, this.session, this.uiResources));
    }

    public void showGameScreen() {
        this.showScreen(new GameScreen(this, this.uiContext, this.session, this.uiResources));
    }

    public void showScreen(Screen newScreen) {
        Screen screen = this.game.getScreen();
        this.game.setScreen(newScreen);
        if (screen != null) {
            screen.dispose();
        }
    }

    private void closeSession() {
        if (this.session != null) {
            this.session.dispose();
            this.session = null;
        }
    }

    @Override
    public void dispose() {
        this.closeSession();
    }
}

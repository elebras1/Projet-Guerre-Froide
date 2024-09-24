package com.populaire.projetguerrefroide;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.populaire.projetguerrefroide.screens.MainMenuScreen;
import com.populaire.projetguerrefroide.ui.CursorManager;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class ProjetGuerreFroide extends Game {
    private Screen screen;
    public static final int WORLD_WIDTH = 5616;
    public static final int WORLD_HEIGHT = 2160;

    @Override
    public void create() {
        CursorManager cursorChanger = new CursorManager();
        cursorChanger.defaultCursor();

        this.screen = new MainMenuScreen(this);
        this.setScreen(this.screen);
    }

    @Override
    public void dispose() {
        this.screen.dispose();
        super.dispose();
    }
}

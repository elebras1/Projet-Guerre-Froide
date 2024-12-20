package com.populaire.projetguerrefroide.service;

import com.badlogic.gdx.assets.AssetManager;
import com.populaire.projetguerrefroide.data.SettingsManager;
import com.populaire.projetguerrefroide.util.Settings;
import com.populaire.projetguerrefroide.ui.CursorManager;

public class GameContext {
    private final SettingsManager settingsManager;
    private final Settings settings;
    private final AssetManager assetManager;
    private final CursorManager cursorManager;

    public GameContext() {
        this.settingsManager = new SettingsManager();
        this.settings = this.settingsManager.loadSettings();
        this.assetManager = new AssetManager();
        this.cursorManager = new CursorManager();
    }

    public Settings getSettings() {
        return this.settings;
    }

    public AssetManager getAssetManager() {
        return this.assetManager;
    }

    public CursorManager getCursorManager() {
        return this.cursorManager;
    }

    public void dispose() {
        this.settingsManager.saveSettings(this.settings);
        this.assetManager.dispose();
        this.cursorManager.dispose();
    }
}

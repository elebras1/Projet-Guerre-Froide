package com.populaire.projetguerrefroide.service;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.populaire.projetguerrefroide.data.SettingsManager;
import com.populaire.projetguerrefroide.ui.CursorManager;

public class GameContext {
    private final SettingsManager settingsManager;
    private final Settings settings;
    private final AssetManager assetManager;
    private final CursorManager cursorManager;
    private final LabelStylePool labelStylePool;

    public GameContext(AssetManager assetManager) {
        this.settingsManager = new SettingsManager();
        this.settings = this.settingsManager.loadSettings();
        this.assetManager = assetManager;
        this.cursorManager = new CursorManager();
        Skin skinFonts = this.assetManager.get("ui/fonts/fonts_skin.json");
        this.labelStylePool = new LabelStylePool(skinFonts);
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

    public LabelStylePool getLabelStylePool() {
        return this.labelStylePool;
    }

    public void dispose() {
        this.settingsManager.saveSettings(this.settings);
        this.assetManager.dispose();
        this.cursorManager.dispose();
    }
}

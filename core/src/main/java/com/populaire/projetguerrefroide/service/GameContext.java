package com.populaire.projetguerrefroide.service;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.populaire.projetguerrefroide.configuration.Settings;
import com.populaire.projetguerrefroide.data.LocalisationManager;
import com.populaire.projetguerrefroide.data.ConfigurationManager;
import com.populaire.projetguerrefroide.ui.CursorManager;

public class GameContext {
    private final ConfigurationManager configurationManager;
    private final LocalisationManager localisationManager;
    private final AssetManager assetManager;
    private final CursorManager cursorManager;
    private final LabelStylePool labelStylePool;
    private final Settings settings;

    public GameContext(AssetManager assetManager) {
        this.configurationManager = new ConfigurationManager();
        this.localisationManager = new LocalisationManager();
        this.assetManager = assetManager;
        this.cursorManager = new CursorManager();
        Skin skinFonts = this.assetManager.get("ui/fonts/fonts_skin.json");
        this.labelStylePool = new LabelStylePool(skinFonts);
        this.settings = this.configurationManager.loadSettings();
        this.localisationManager.setLanguage(this.settings.getLanguage());
    }

    public ConfigurationManager getConfigurationManager() {
        return this.configurationManager;
    }

    public Settings getSettings() {
        return this.settings;
    }

    public LocalisationManager getLocalisationManager() {
        return this.localisationManager;
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
        this.configurationManager.saveSettings(this.settings);
        this.assetManager.dispose();
        this.cursorManager.dispose();
    }
}

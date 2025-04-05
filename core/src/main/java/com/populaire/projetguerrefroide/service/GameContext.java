package com.populaire.projetguerrefroide.service;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.tommyettinger.ds.ObjectObjectMap;
import com.populaire.projetguerrefroide.configuration.Settings;
import com.populaire.projetguerrefroide.dao.LocalisationDao;
import com.populaire.projetguerrefroide.dao.ConfigurationDao;
import com.populaire.projetguerrefroide.ui.CursorManager;

import java.util.Map;

public class GameContext {
    private final ConfigurationDao configurationManager;
    private final LocalisationDao localisationManager;
    private final AssetManager assetManager;
    private final CursorManager cursorManager;
    private final LabelStylePool labelStylePool;
    private final Map<String, String> localisation;
    private Settings settings;

    public GameContext(AssetManager assetManager) {
        this.configurationManager = new ConfigurationDao();
        this.localisationManager = new LocalisationDao();
        this.assetManager = assetManager;
        this.cursorManager = new CursorManager();
        Skin skinFonts = this.assetManager.get("ui/fonts/fonts_skin.json");
        this.settings = this.configurationManager.loadSettings();
        this.labelStylePool = new LabelStylePool(skinFonts, this.settings.getLanguage());
        this.localisationManager.setLanguage(this.settings.getLanguage());
        this.localisation = new ObjectObjectMap<>();
    }

    public ConfigurationDao getConfigurationManager() {
        return this.configurationManager;
    }

    public LocalisationDao getLocalisationManager() {
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

    public Settings getSettings() {
        return this.settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public Map<String, String> getLocalisation() {
        return this.localisation;
    }

    public void putAllLocalisation(Map<String, String> localisation) {
        this.localisation.putAll(localisation);
    }

    public void dispose() {
        this.configurationManager.saveSettings(this.settings);
        this.assetManager.dispose();
        this.cursorManager.dispose();
    }
}

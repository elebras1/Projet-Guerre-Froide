package com.populaire.projetguerrefroide.service;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import io.github.elebras1.flecs.World;
import com.monstrous.gdx.webgpu.assets.WgAssetManager;
import com.populaire.projetguerrefroide.configuration.Settings;
import com.populaire.projetguerrefroide.dao.ConfigurationDao;
import com.populaire.projetguerrefroide.dao.LocalisationDao;
import com.populaire.projetguerrefroide.dao.impl.ConfigurationDaoImpl;
import com.populaire.projetguerrefroide.dao.impl.LocalisationDaoImpl;
import com.populaire.projetguerrefroide.pojo.Bookmark;
import com.populaire.projetguerrefroide.ui.widget.CursorManager;

public class ConfigurationService {
    private final ConfigurationDao configurationDao;
    private final LocalisationDao localisationDao;

    public ConfigurationService() {
        this.configurationDao = new ConfigurationDaoImpl();
        this.localisationDao = new LocalisationDaoImpl();
    }

    public GameContext getGameContext(World ecsWorld) {
        Bookmark bookmark = this.configurationDao.loadBookmark();
        AssetManager assetManager = new WgAssetManager();
        this.loadInitialAssets(assetManager);
        CursorManager cursorManager = new CursorManager();
        Settings settings = this.configurationDao.loadSettings();
        Skin skinFonts = assetManager.get("generated-skins/fonts/fonts.json");
        LabelStylePool labelStylePool = new LabelStylePool(skinFonts, settings.getLanguage());
        return new GameContext(ecsWorld, bookmark, assetManager, cursorManager, settings, labelStylePool);
    }

    public void loadInitialAssets(AssetManager assetManager) {
        assetManager.load("generated-skins/ui/ui.json", Skin.class);
        assetManager.load("generated-skins/fonts/fonts.json", Skin.class);
        assetManager.load("generated-skins/ui/scrollbars/scrollbars.json", Skin.class);
        assetManager.load("generated-skins/ui/mainmenu/mainmenu.json", Skin.class);
        assetManager.finishLoading();
    }

    public void loadGameAssets(AssetManager assetManager) {
        assetManager.load("generated-skins/ui/newgame/newgame.json", Skin.class);
        assetManager.load("generated-skins/flags/flags.json", Skin.class);
        assetManager.load("generated-skins/portraits/portraits.json", Skin.class);
        assetManager.load("generated-skins/ui/mainmenu_ig/mainmenu_ig.json", Skin.class);
        assetManager.load("generated-skins/ui/popup/popup.json", Skin.class);
        assetManager.load("generated-skins/ui/topbar/topbar.json", Skin.class);
        assetManager.load("generated-skins/ui/minimap/minimap.json", Skin.class);
        assetManager.load("generated-skins/ui/province/province.json", Skin.class);
        assetManager.load("generated-skins/ui/economy/economy.json", Skin.class);
    }

    public void loadMainMenuLocalisation(GameContext gameContext) {
        this.setLanguage(gameContext);
        gameContext.putAllLocalisation(this.localisationDao.readMainMenu());
        gameContext.putAllLocalisation(this.localisationDao.readCountries());

    }

    public void loadNewGameLocalisation(GameContext gameContext) {
        this.setLanguage(gameContext);
        gameContext.putAllLocalisation(this.localisationDao.readNewgame());
        gameContext.putAllLocalisation(this.localisationDao.readBookmark());
        gameContext.putAllLocalisation(this.localisationDao.readPolitics());
        gameContext.putAllLocalisation(this.localisationDao.readMainMenuInGame());
        gameContext.putAllLocalisation(this.localisationDao.readPopup());
        gameContext.putAllLocalisation(this.localisationDao.readProvinces());
        gameContext.putAllLocalisation(this.localisationDao.readLanguage());
    }

    public void loadGameLocalisation(GameContext gameContext) {
        this.setLanguage(gameContext);
        gameContext.putAllLocalisation(this.localisationDao.readPolitics());
        gameContext.putAllLocalisation(this.localisationDao.readMainMenuInGame());
        gameContext.putAllLocalisation(this.localisationDao.readPopup());
        gameContext.putAllLocalisation(this.localisationDao.readProvinces());
        gameContext.putAllLocalisation(this.localisationDao.readRegions());
        gameContext.putAllLocalisation(this.localisationDao.readLanguage());
        gameContext.putAllLocalisation(this.localisationDao.readInterface());
        gameContext.putAllLocalisation(this.localisationDao.readEconomy());
    }

    private void setLanguage(GameContext gameContext) {
        this.localisationDao.setLanguage(gameContext.getSettings().getLanguage());
    }

    public void saveSettings(Settings settings) {
        this.configurationDao.saveSettings(settings);
    }
}

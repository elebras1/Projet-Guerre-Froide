package com.populaire.projetguerrefroide.service;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.monstrous.gdx.webgpu.assets.WgAssetManager;
import com.populaire.projetguerrefroide.configuration.Settings;
import com.populaire.projetguerrefroide.dao.ConfigurationDao;
import com.populaire.projetguerrefroide.dao.LocalisationDao;
import com.populaire.projetguerrefroide.dao.impl.ConfigurationDaoImpl;
import com.populaire.projetguerrefroide.dao.impl.LocalisationDaoImpl;
import com.populaire.projetguerrefroide.entity.Bookmark;
import com.populaire.projetguerrefroide.ui.widget.CursorManager;

public class ConfigurationService {
    private final ConfigurationDao configurationDao;
    private final LocalisationDao localisationDao;

    public ConfigurationService() {
        this.configurationDao = new ConfigurationDaoImpl();
        this.localisationDao = new LocalisationDaoImpl();
    }

    public GameContext getGameContext() {
        Bookmark bookmark = this.configurationDao.loadBookmark();
        AssetManager assetManager = new WgAssetManager();
        this.loadInitialAssets(assetManager);
        CursorManager cursorManager = new CursorManager();
        Settings settings = this.configurationDao.loadSettings();
        Skin skinFonts = assetManager.get("ui/fonts/fonts_skin.json");
        LabelStylePool labelStylePool = new LabelStylePool(skinFonts, settings.getLanguage());
        return new GameContext(bookmark, assetManager, cursorManager, settings, labelStylePool);
    }

    public void loadInitialAssets(AssetManager assetManager) {
        assetManager.load("ui/ui_skin.json", Skin.class);
        assetManager.load("ui/fonts/fonts_skin.json", Skin.class);
        assetManager.load("ui/scrollbars/scrollbars_skin.json", Skin.class);
        assetManager.load("ui/mainmenu/mainmenu_skin.json", Skin.class);
        assetManager.finishLoading();
    }

    public void loadGameAssets(AssetManager assetManager) {
        assetManager.load("ui/newgame/newgame_skin.json", Skin.class);
        assetManager.load("flags/flags_skin.json", Skin.class);
        assetManager.load("portraits/portraits_skin.json", Skin.class);
        assetManager.load("ui/mainmenu_ig/mainmenu_ig_skin.json", Skin.class);
        assetManager.load("ui/popup/popup_skin.json", Skin.class);
        assetManager.load("ui/topbar/topbar_skin.json", Skin.class);
        assetManager.load("ui/minimap/minimap_skin.json", Skin.class);
        assetManager.load("ui/province/province_skin.json", Skin.class);
    }

    public void loadMainMenuLocalisation(GameContext gameContext) {
        this.setLanguage(gameContext);
        gameContext.putAllLocalisation(this.localisationDao.readMainMenuCsv());
        gameContext.putAllLocalisation(this.localisationDao.readCountriesCsv());

    }

    public void loadNewGameLocalisation(GameContext gameContext) {
        this.setLanguage(gameContext);
        gameContext.putAllLocalisation(this.localisationDao.readNewgameCsv());
        gameContext.putAllLocalisation(this.localisationDao.readBookmarkCsv());
        gameContext.putAllLocalisation(this.localisationDao.readPoliticsCsv());
        gameContext.putAllLocalisation(this.localisationDao.readMainMenuInGameCsv());
        gameContext.putAllLocalisation(this.localisationDao.readPopupCsv());
        gameContext.putAllLocalisation(this.localisationDao.readProvincesCsv());
        gameContext.putAllLocalisation(this.localisationDao.readLanguageCsv());
    }

    public void loadGameLocalisation(GameContext gameContext) {
        this.setLanguage(gameContext);
        gameContext.putAllLocalisation(this.localisationDao.readPoliticsCsv());
        gameContext.putAllLocalisation(this.localisationDao.readMainMenuInGameCsv());
        gameContext.putAllLocalisation(this.localisationDao.readPopupCsv());
        gameContext.putAllLocalisation(this.localisationDao.readProvincesCsv());
        gameContext.putAllLocalisation(this.localisationDao.readRegionsCsv());
        gameContext.putAllLocalisation(this.localisationDao.readLanguageCsv());
        gameContext.putAllLocalisation(this.localisationDao.readInterfaceCsv());
    }

    private void setLanguage(GameContext gameContext) {
        this.localisationDao.setLanguage(gameContext.getSettings().getLanguage());
    }

    public void saveSettings(Settings settings) {
        this.configurationDao.saveSettings(settings);
    }
}

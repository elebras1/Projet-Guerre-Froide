package com.populaire.projetguerrefroide.service;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ObjectMap;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import io.github.elebras1.flecs.World;
import com.monstrous.gdx.webgpu.assets.WgAssetManager;
import com.populaire.projetguerrefroide.configuration.Settings;
import com.populaire.projetguerrefroide.dao.ConfigurationDao;
import com.populaire.projetguerrefroide.dao.LocalisationDao;
import com.populaire.projetguerrefroide.dao.impl.ConfigurationDaoImpl;
import com.populaire.projetguerrefroide.dao.impl.LocalisationDaoImpl;
import com.populaire.projetguerrefroide.pojo.Bookmark;
import com.populaire.projetguerrefroide.ui.widget.CursorManager;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigurationService {
    private static final List<String> LANGUAGES_WITH_OWN_FONTS = List.of("RUSSIAN", "CHINESE", "ARABIC");
    private static final Pattern FONT_SIZE = Pattern.compile("(_[0-9]{2})");
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
        Skin skinFonts = assetManager.get(Skins.FONTS);
        this.applyLanguageFonts(skinFonts, settings.getLanguage());
        return new GameContext(ecsWorld, bookmark, assetManager, cursorManager, settings, skinFonts);
    }

    private void applyLanguageFonts(Skin skinFonts, String language) {
        if (!LANGUAGES_WITH_OWN_FONTS.contains(language)) {
            return;
        }

        for (ObjectMap.Entry<String, LabelStyle> style : skinFonts.getAll(LabelStyle.class)) {
            String languageFont = this.languageFont(style.key, language);
            if (skinFonts.has(languageFont, BitmapFont.class)) {
                style.value.font = skinFonts.getFont(languageFont);
            }
        }
    }

    private String languageFont(String styleName, String language) {
        String fontSize = "_16";
        Matcher matcher = FONT_SIZE.matcher(styleName);
        if (matcher.find()) {
            fontSize = matcher.group(1);
        }

        return language.toLowerCase() + fontSize;
    }

    public void loadInitialAssets(AssetManager assetManager) {
        assetManager.load(Skins.UI, Skin.class);
        assetManager.load(Skins.FONTS, Skin.class);
        assetManager.load(Skins.UI_SCROLLBARS, Skin.class);
        assetManager.load(Skins.UI_MAINMENU, Skin.class);
        assetManager.finishLoading();
    }

    public void loadGameAssets(AssetManager assetManager) {
        assetManager.load(Skins.UI_NEWGAME, Skin.class);
        assetManager.load(Skins.FLAGS, Skin.class);
        assetManager.load(Skins.PORTRAITS, Skin.class);
        assetManager.load(Skins.UI_MAINMENU_IG, Skin.class);
        assetManager.load(Skins.UI_POPUP, Skin.class);
        assetManager.load(Skins.UI_TOPBAR, Skin.class);
        assetManager.load(Skins.UI_MINIMAP, Skin.class);
        assetManager.load(Skins.UI_PROVINCE, Skin.class);
        assetManager.load(Skins.UI_ECONOMY, Skin.class);
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
        gameContext.putAllLocalisation(this.localisationDao.readUnits());
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

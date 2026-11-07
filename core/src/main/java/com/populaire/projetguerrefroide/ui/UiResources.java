package com.populaire.projetguerrefroide.ui;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ObjectMap;
import com.populaire.projetguerrefroide.configuration.Settings;
import com.monstrous.gdx.webgpu.assets.WgAssetManager;
import com.populaire.projetguerrefroide.dao.ConfigurationDao;
import com.populaire.projetguerrefroide.dao.LocalisationDao;
import com.populaire.projetguerrefroide.dao.impl.ConfigurationDaoImpl;
import com.populaire.projetguerrefroide.dao.impl.LocalisationDaoImpl;
import com.populaire.projetguerrefroide.configuration.Bookmark;
import com.populaire.projetguerrefroide.configuration.TutorialCategory;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.widget.CursorManager;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UiResources {
    private static final List<String> LANGUAGES_WITH_OWN_FONTS = List.of("RUSSIAN", "CHINESE", "ARABIC");
    private static final Pattern FONT_SIZE = Pattern.compile("(_[0-9]{2})");
    private final ConfigurationDao configurationDao;
    private final LocalisationDao localisationDao;

    public UiResources() {
        this.configurationDao = new ConfigurationDaoImpl();
        this.localisationDao = new LocalisationDaoImpl();
    }

    public UiContext getUiContext() {
        Bookmark bookmark = this.configurationDao.loadBookmark();
        AssetManager assetManager = new WgAssetManager();
        this.loadInitialAssets(assetManager);
        CursorManager cursorManager = new CursorManager();
        Settings settings = this.configurationDao.loadSettings();
        Skin skinFonts = assetManager.get(Skins.FONTS);
        this.applyLanguageFonts(skinFonts, settings.getLanguage());
        return new UiContext(bookmark, assetManager, cursorManager, settings, skinFonts);
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
        assetManager.load(Skins.UI_NEWGAME, Skin.class);
        assetManager.finishLoading();
    }

    public void loadGameAssets(AssetManager assetManager) {
        assetManager.load(Skins.FLAGS, Skin.class);
        assetManager.load(Skins.PORTRAITS, Skin.class);
        assetManager.load(Skins.UI_MAINMENU_IG, Skin.class);
        assetManager.load(Skins.UI_POPUP, Skin.class);
        assetManager.load(Skins.UI_TOPBAR, Skin.class);
        assetManager.load(Skins.UI_MINIMAP, Skin.class);
        assetManager.load(Skins.UI_PROVINCE, Skin.class);
        assetManager.load(Skins.UI_ECONOMY, Skin.class);
    }

    public void loadMainMenuLocalisation(UiContext uiContext) {
        this.setLanguage(uiContext);
        uiContext.putAllLocalisation(this.localisationDao.readMainMenu());
        uiContext.putAllLocalisation(this.localisationDao.readMainMenuInGame());
        uiContext.putAllLocalisation(this.localisationDao.readLanguage());
        uiContext.putAllLocalisation(this.localisationDao.readCountries());
        uiContext.putAllLocalisation(this.localisationDao.readCredits());
    }

    public void loadNewGameLocalisation(UiContext uiContext) {
        this.setLanguage(uiContext);
        uiContext.putAllLocalisation(this.localisationDao.readNewgame());
        uiContext.putAllLocalisation(this.localisationDao.readBookmark());
        uiContext.putAllLocalisation(this.localisationDao.readPolitics());
        uiContext.putAllLocalisation(this.localisationDao.readMainMenuInGame());
        uiContext.putAllLocalisation(this.localisationDao.readPopup());
        uiContext.putAllLocalisation(this.localisationDao.readProvinces());
        uiContext.putAllLocalisation(this.localisationDao.readLanguage());
    }

    public void loadGameLocalisation(UiContext uiContext) {
        this.setLanguage(uiContext);
        uiContext.putAllLocalisation(this.localisationDao.readPolitics());
        uiContext.putAllLocalisation(this.localisationDao.readMainMenuInGame());
        uiContext.putAllLocalisation(this.localisationDao.readPopup());
        uiContext.putAllLocalisation(this.localisationDao.readProvinces());
        uiContext.putAllLocalisation(this.localisationDao.readRegions());
        uiContext.putAllLocalisation(this.localisationDao.readUnits());
        uiContext.putAllLocalisation(this.localisationDao.readLanguage());
        uiContext.putAllLocalisation(this.localisationDao.readInterface());
        uiContext.putAllLocalisation(this.localisationDao.readEconomy());
    }

    private void setLanguage(UiContext uiContext) {
        this.localisationDao.setLanguage(uiContext.getSettings().getLanguage());
    }

    public List<TutorialCategory> loadTutorials() {
        return this.configurationDao.loadTutorials();
    }

    public void saveSettings(Settings settings) {
        this.configurationDao.saveSettings(settings);
    }
}

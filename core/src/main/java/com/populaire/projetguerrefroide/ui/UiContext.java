package com.populaire.projetguerrefroide.ui;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import com.github.tommyettinger.ds.ObjectObjectMap;
import com.populaire.projetguerrefroide.configuration.Settings;
import com.populaire.projetguerrefroide.pojo.Bookmark;
import com.populaire.projetguerrefroide.ui.widget.ColorDrawablePool;
import com.populaire.projetguerrefroide.ui.widget.CursorManager;

import java.util.Map;

public class UiContext implements Disposable {
    private final Bookmark bookmark;
    private final AssetManager assetManager;
    private final CursorManager cursorManager;
    private final Skin skinFonts;
    private final ColorDrawablePool colorTexturePool;
    private final Map<String, String> localisation;
    private Settings settings;

    public UiContext(Bookmark bookmark, AssetManager assetManager, CursorManager cursorManager, Settings settings, Skin skinFonts) {
        this.bookmark = bookmark;
        this.assetManager = assetManager;
        this.cursorManager = cursorManager;
        this.skinFonts = skinFonts;
        this.colorTexturePool = new ColorDrawablePool();
        this.localisation = new ObjectObjectMap<>();
        this.settings = settings;
    }

    public Bookmark getBookmark() {
        return this.bookmark;
    }

    public AssetManager getAssetManager() {
        return this.assetManager;
    }

    public CursorManager getCursorManager() {
        return this.cursorManager;
    }

    public Skin getSkinFonts() {
        return this.skinFonts;
    }

    public ColorDrawablePool getColorTexturePool() {
        return this.colorTexturePool;
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

    @Override
    public void dispose() {
        this.assetManager.dispose();
        this.cursorManager.dispose();
    }
}

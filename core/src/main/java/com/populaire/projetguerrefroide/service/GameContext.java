package com.populaire.projetguerrefroide.service;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Disposable;
import com.github.elebras1.flecs.World;
import com.github.tommyettinger.ds.ObjectObjectMap;
import com.populaire.projetguerrefroide.configuration.Settings;
import com.populaire.projetguerrefroide.entity.Bookmark;
import com.populaire.projetguerrefroide.ui.widget.CursorManager;

import java.util.Map;

public class GameContext implements Disposable {
    private final World ecsWorld;
    private final Bookmark bookmark;
    private final AssetManager assetManager;
    private final CursorManager cursorManager;
    private final LabelStylePool labelStylePool;
    private final Map<String, String> localisation;
    private Settings settings;

    public GameContext(World ecsWorld, Bookmark bookmark, AssetManager assetManager, CursorManager cursorManager, Settings settings, LabelStylePool labelStylePool) {
        this.ecsWorld = ecsWorld;
        this.bookmark = bookmark;
        this.assetManager = assetManager;
        this.cursorManager = cursorManager;
        this.labelStylePool = labelStylePool;
        this.localisation = new ObjectObjectMap<>();
        this.settings = settings;
    }

    public World getEcsWorld() {
        return this.ecsWorld;
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

    @Override
    public void dispose() {
        this.assetManager.dispose();
        this.cursorManager.dispose();
    }
}

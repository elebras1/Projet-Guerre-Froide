package com.populaire.projetguerrefroide.service;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.github.tommyettinger.ds.IntObjectMap;
import com.monstrous.gdx.webgpu.graphics.WgTexture;

public class ColorDrawablePool implements Disposable {

    private final IntObjectMap<Drawable> drawableCache;
    private final Texture whiteTexture;
    private final TextureRegionDrawable whiteDrawable;

    public ColorDrawablePool() {
        this.drawableCache = new IntObjectMap<>();
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        this.whiteTexture = new WgTexture(pixmap);
        pixmap.dispose();

        this.whiteDrawable = new TextureRegionDrawable(this.whiteTexture);
    }

    public Drawable get(int colorRgba) {
        if (this.drawableCache.containsKey(colorRgba)) {
            return this.drawableCache.get(colorRgba);
        }

        Color colorObj = new Color();
        Color.rgba8888ToColor(colorObj, colorRgba);
        Drawable tinted = this.whiteDrawable.tint(colorObj);
        this.drawableCache.put(colorRgba, tinted);
        return tinted;
    }

    @Override
    public void dispose() {
        this.whiteTexture.dispose();
        this.drawableCache.clear();
    }
}

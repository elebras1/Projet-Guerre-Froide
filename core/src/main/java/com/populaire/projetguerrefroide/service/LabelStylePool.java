package com.populaire.projetguerrefroide.service;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.tommyettinger.ds.ObjectObjectMap;

import java.util.Map;

public class LabelStylePool {
    private final Map<String, LabelStyle> labelStyles;
    private final Skin skinFonts;

    public LabelStylePool(Skin skinFonts) {
        this.labelStyles = new ObjectObjectMap<>();
        this.skinFonts = skinFonts;
    }

    private void addLabelStyle(String name) {
        LabelStyle labelStyle = new LabelStyle();
        labelStyle.font = this.skinFonts.getFont(name);
        this.labelStyles.put(name, labelStyle);
    }

    public LabelStyle getLabelStyle(String name) {
        if (!this.skinFonts.has(name, BitmapFont.class)) {
            throw new IllegalArgumentException("Font with name '" + name + "' not found in Skin.");
        }

        if (!this.labelStyles.containsKey(name)) {
            this.addLabelStyle(name);
        }

        return this.labelStyles.get(name);
    }
}

package com.populaire.projetguerrefroide.service;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.tommyettinger.ds.ObjectObjectMap;

import java.util.Map;

public class LabelStylePool {
    private final Map<String, LabelStyle> labelStyles;
    private final Map<String, Color> colors;
    private final Skin skinFonts;

    public LabelStylePool(Skin skinFonts) {
        this.labelStyles = new ObjectObjectMap<>();
        this.colors = new ObjectObjectMap<>();
        this.skinFonts = skinFonts;
    }

    private void addLabelStyle(String name) {
        LabelStyle labelStyle = new LabelStyle();
        labelStyle.font = this.skinFonts.getFont(name);
        this.labelStyles.put(name, labelStyle);
    }

    private void addLabelStyle(String name, String color) {
        LabelStyle labelStyle = new LabelStyle();
        labelStyle.font = this.skinFonts.getFont(name);
        labelStyle.fontColor = this.getColor(color);
        this.labelStyles.put(name + "_" + color, labelStyle);
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

    public LabelStyle getLabelStyle(String name, String color) {
        if (!this.skinFonts.has(name, BitmapFont.class)) {
            throw new IllegalArgumentException("Font with name '" + name + "' not found in Skin.");
        }

        if (!this.labelStyles.containsKey(name + "_" + color)) {
            this.addLabelStyle(name, color);
        }

        return this.labelStyles.get(name + "_" + color);
    }

    private void addColor(String name) {
        this.colors.put(name, this.skinFonts.getColor(name));
    }

    private Color getColor(String name) {
        if (!this.skinFonts.has(name, Color.class)) {
            throw new IllegalArgumentException("Color with name '" + name + "' not found in Skin.");
        }

        if (!this.colors.containsKey(name)) {
            this.addColor(name);
        }

        return this.colors.get(name);
    }
}
